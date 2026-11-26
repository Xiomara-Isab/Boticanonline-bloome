package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.service.CategoriaService;
import com.botica.service.ReporteService;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * GRASP Controlador de las funciones del Administrador sobre el catálogo y las ventas:
 * /admin/categorias (crear, renombrar, activar/desactivar y eliminar) y /admin/reportes (solo lectura).
 * Las reglas están en CategoriaService y ReporteService; aquí solo se leen los parámetros y se elige la vista.
 */
@WebServlet({"/admin/categorias", "/admin/reportes"})
public class AdminController extends HttpServlet {
    private final CategoriaService categoriaService = new CategoriaService();
    private final ReporteService reporteService = new ReporteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!esAdministrador(req, resp)) return;
        if ("/admin/reportes".equals(req.getServletPath())) {
            mostrarReportes(req, resp);
        } else {
            req.setAttribute("categorias", categoriaService.listar());
            req.getRequestDispatcher("/WEB-INF/admin/categorias.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!esAdministrador(req, resp)) return;
        HttpSession sesion = req.getSession();
        String accion = req.getParameter("accion") == null ? "" : req.getParameter("accion");
        try {
            switch (accion) {
                case "crear" -> {
                    categoriaService.crear(req.getParameter("nombre"));
                    sesion.setAttribute("flashOk", "Categoría creada");
                }
                case "renombrar" -> {
                    categoriaService.renombrar(Integer.parseInt(req.getParameter("id")), req.getParameter("nombre"));
                    sesion.setAttribute("flashOk", "Categoría renombrada. Sus productos ya usan el nombre nuevo");
                }
                case "cambiarActiva" -> {
                    boolean activa = Boolean.parseBoolean(req.getParameter("activa"));
                    categoriaService.cambiarActiva(Integer.parseInt(req.getParameter("id")), activa);
                    sesion.setAttribute("flashOk", activa ? "Categoría activada" : "Categoría desactivada: ya no se ofrece para productos nuevos");
                }
                case "eliminar" -> {
                    categoriaService.eliminar(Integer.parseInt(req.getParameter("id")));
                    sesion.setAttribute("flashOk", "Categoría eliminada");
                }
                default -> sesion.setAttribute("flashErr", "Acción no reconocida");
            }
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
        } catch (RuntimeException e) {
            sesion.setAttribute("flashErr", "No se pudo completar la acción. Revisa los datos ingresados");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categorias");
    }

    /** Por defecto muestra los últimos 30 días; con fechas inválidas avisa y vuelve a ese rango. */
    private void mostrarReportes(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(29);
        LocalDate hasta = hoy;
        try {
            if (tieneValor(req.getParameter("desde"))) desde = LocalDate.parse(req.getParameter("desde"));
            if (tieneValor(req.getParameter("hasta"))) hasta = LocalDate.parse(req.getParameter("hasta"));
            req.setAttribute("ventas", reporteService.ventasPorRango(desde, hasta));
        } catch (DateTimeParseException | ReglaNegocioException e) {
            req.setAttribute("error", e instanceof ReglaNegocioException ? e.getMessage() : "Fecha no válida");
            desde = hoy.minusDays(29);
            hasta = hoy;
            req.setAttribute("ventas", reporteService.ventasPorRango(desde, hasta));
        }
        req.setAttribute("hoy", reporteService.ventasDelDia());
        req.setAttribute("masVendidos", reporteService.masVendidos(desde, hasta));
        req.getRequestDispatcher("/WEB-INF/admin/reportes.jsp").forward(req, resp);
    }

    // shortcut: el permiso se revisa aquí porque las rutas /admin/* aún no están en el mapa de RolFilter (Integrante 2)
    private boolean esAdministrador(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?sesion=1");
            return false;
        }
        if (!Constantes.ROL_ADMINISTRADOR.equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.isBlank();
    }
}
