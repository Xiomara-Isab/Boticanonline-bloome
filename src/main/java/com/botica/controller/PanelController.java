package com.botica.controller;

import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.patterns.facade.PedidoFacade;
import com.botica.service.ProductoService;
import com.botica.service.ReclamoService;
import com.botica.service.UsuarioService;
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

/**
 * GRASP Controlador del panel por rol: GET muestra la vista del rol; POST valida el permiso del actor
 * y delega la regla de negocio en el servicio correspondiente.
 */
@WebServlet("/panel")
public class PanelController extends HttpServlet {
    private final ProductoService productoService = new ProductoService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final ReclamoService reclamoService = new ReclamoService();
    private final PedidoFacade pedidoFacade = new PedidoFacade();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = usuarioActual(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        String vista = switch (u.getRol()) {
            case Constantes.ROL_VENDEDOR -> "vendedor";
            case Constantes.ROL_ALMACEN -> "almacen";
            case Constantes.ROL_SOPORTE -> "soporte";
            case Constantes.ROL_ADMINISTRADOR -> "admin";
            default -> "cliente";
        };
        req.getRequestDispatcher("/WEB-INF/panel/" + vista + ".jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Usuario u = usuarioActual(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        HttpSession sesion = req.getSession();
        String accion = req.getParameter("accion");
        if (accion == null) accion = "";
        try {
            switch (accion) {
                case "crearProducto" -> {
                    exigir(u, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
                    productoService.crear(new Producto(
                            req.getParameter("nombre").trim(),
                            req.getParameter("descripcion"),
                            Double.parseDouble(req.getParameter("precio")),
                            Integer.parseInt(req.getParameter("stock")),
                            req.getParameter("categoria"),
                            fechaOpcional(req.getParameter("fechaVencimiento")),
                            u.getId()));
                    sesion.setAttribute("flashOk", "Producto agregado al catálogo");
                }
                case "actualizarProducto" -> {
                    exigir(u, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
                    productoService.actualizarPrecioYStock(Integer.parseInt(req.getParameter("id")),
                            Double.parseDouble(req.getParameter("precio")),
                            Integer.parseInt(req.getParameter("stock")));
                    sesion.setAttribute("flashOk", "Producto actualizado");
                }
                case "ajustarStock" -> {
                    exigir(u, Constantes.ROL_ALMACEN, Constantes.ROL_ADMINISTRADOR);
                    productoService.ajustarStock(Integer.parseInt(req.getParameter("id")),
                            Integer.parseInt(req.getParameter("stock")));
                    sesion.setAttribute("flashOk", "Stock actualizado");
                }
                case "eliminarProducto" -> {
                    exigir(u, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
                    productoService.eliminar(Integer.parseInt(req.getParameter("id")));
                    sesion.setAttribute("flashOk", "Producto eliminado");
                }
                case "crearReclamo" -> {
                    exigir(u, Constantes.ROL_CLIENTE);
                    pedidoFacade.registrarReclamo(u.getId(), Integer.parseInt(req.getParameter("idPedido")),
                            req.getParameter("descripcion"));
                    sesion.setAttribute("flashOk", "Reclamo enviado. Soporte te atenderá pronto");
                }
                case "estadoReclamo" -> {
                    exigir(u, Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
                    reclamoService.cambiarEstado(Integer.parseInt(req.getParameter("id")), req.getParameter("estado"));
                    sesion.setAttribute("flashOk", "Reclamo actualizado");
                }
                case "cambiarRol" -> {
                    exigir(u, Constantes.ROL_ADMINISTRADOR);
                    String nuevoRol = req.getParameter("rol");
                    usuarioService.cambiarRol(u.getId(), Integer.parseInt(req.getParameter("id")), nuevoRol);
                    sesion.setAttribute("flashOk", "Rol actualizado a " + nuevoRol);
                }
                case "crearUsuario" -> {
                    exigir(u, Constantes.ROL_ADMINISTRADOR);
                    String rol = req.getParameter("rol");
                    usuarioService.crearPersonal(req.getParameter("nombre"), req.getParameter("correo"),
                            req.getParameter("contrasena"), rol);
                    sesion.setAttribute("flashOk", "Usuario creado con rol " + rol);
                }
                default -> sesion.setAttribute("flashErr", "Acción no reconocida");
            }
        } catch (SecurityException e) {
            sesion.setAttribute("flashErr", "No tienes permiso para realizar esta acción");
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
        } catch (RuntimeException e) {
            sesion.setAttribute("flashErr", "No se pudo completar la acción. Revisa los datos ingresados");
        }
        resp.sendRedirect(req.getContextPath() + "/panel");
    }

    private Usuario usuarioActual(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
    }

    private void exigir(Usuario u, String... roles) {
        for (String r : roles) {
            if (r.equals(u.getRol())) return;
        }
        throw new SecurityException("Rol sin permiso");
    }

    private LocalDate fechaOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : LocalDate.parse(valor);
    }
}
