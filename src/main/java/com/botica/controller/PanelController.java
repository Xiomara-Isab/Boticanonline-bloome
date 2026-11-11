package com.botica.controller;

import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.patterns.command.Comando;
import com.botica.patterns.command.HistorialComandos;
import com.botica.patterns.facade.PedidoFacade;
import com.botica.patterns.proxy.ProductoServiceProxy;
import com.botica.service.ProductoService;
import com.botica.service.ProductoServiceImpl;
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
 * y delega la regla de negocio en el servicio correspondiente. Las acciones del catálogo pasan por
 * ProductoServiceProxy, que revisa el rol antes de llegar al servicio real.
 */
@WebServlet("/panel")
public class PanelController extends HttpServlet {
    private final ProductoService productoServiceReal = new ProductoServiceImpl();
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
        ProductoService productoService = new ProductoServiceProxy(productoServiceReal, u);
        try {
            switch (accion) {
                case "crearProducto" -> {
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
                    productoService.actualizarPrecioYStock(Integer.parseInt(req.getParameter("id")),
                            Double.parseDouble(req.getParameter("precio")),
                            Integer.parseInt(req.getParameter("stock")));
                    sesion.setAttribute("flashOk", "Producto actualizado");
                }
                case "ajustarStock" -> {
                    productoService.ajustarStock(Integer.parseInt(req.getParameter("id")),
                            Integer.parseInt(req.getParameter("stock")));
                    sesion.setAttribute("flashOk", "Stock actualizado");
                }
                case "eliminarProducto" -> {
                    productoService.eliminar(Integer.parseInt(req.getParameter("id")));
                    sesion.setAttribute("flashOk", "Producto eliminado");
                }
                case "cambiarActivoProducto" -> {
                    boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                    productoService.cambiarActivo(Integer.parseInt(req.getParameter("id")), activo);
                    sesion.setAttribute("flashOk", activo ? "Producto activado: vuelve a verse en el catálogo"
                            : "Producto desactivado: ya no se muestra en el catálogo");
                }
                case "aplicarDescuento" -> {
                    productoService.aplicarDescuento(Integer.parseInt(req.getParameter("id")),
                            Integer.parseInt(req.getParameter("descuento")));
                    sesion.setAttribute("flashOk", "Descuento actualizado");
                }
                case "registrarIngreso" -> {
                    productoService.registrarIngreso(Integer.parseInt(req.getParameter("id")),
                            Integer.parseInt(req.getParameter("cantidad")));
                    sesion.setAttribute("flashOk", "Ingreso de mercadería registrado");
                }
                case "retirarVencidos" -> {
                    int retirados = productoService.retirarVencidos();
                    sesion.setAttribute("flashOk", retirados == 0
                            ? "No hay productos vencidos con stock para retirar"
                            : retirados + " producto(s) vencido(s) retirado(s) del stock");
                }
                case "crearReclamo" -> {
                    exigir(u, Constantes.ROL_CLIENTE);
                    pedidoFacade.registrarReclamo(u.getId(), Integer.parseInt(req.getParameter("idPedido")),
                            req.getParameter("descripcion"));
                    sesion.setAttribute("flashOk", "Reclamo enviado. Soporte te atenderá pronto");
                }
                case "estadoReclamo" -> {
                    exigir(u, Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
                    reclamoService.cambiarEstado(Integer.parseInt(req.getParameter("id")), req.getParameter("estado"),
                            historialReclamos(sesion));
                    sesion.setAttribute("flashOk", "Reclamo actualizado");
                }
                case "responderReclamo" -> {
                    exigir(u, Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
                    reclamoService.responder(u.getId(), Integer.parseInt(req.getParameter("id")),
                            req.getParameter("respuesta"));
                    sesion.setAttribute("flashOk", "Respuesta enviada al cliente");
                }
                case "reembolsarPedido" -> {
                    exigir(u, Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
                    var reembolso = pedidoFacade.reembolsarPedido(Integer.parseInt(req.getParameter("idPedido")));
                    sesion.setAttribute("flashOk", "Pedido #" + reembolso.getIdPedido() + " reembolsado ("
                            + com.botica.util.Html.money(-reembolso.getMonto()) + "). El stock volvió al inventario");
                }
                case "deshacerReclamo" -> {
                    exigir(u, Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
                    Comando deshecho = historialReclamos(sesion).deshacer();
                    sesion.setAttribute("flashOk", "Deshecho: " + deshecho.descripcion());
                }
                case "cambiarRol" -> {
                    exigir(u, Constantes.ROL_ADMINISTRADOR);
                    String nuevoRol = req.getParameter("rol");
                    usuarioService.cambiarRol(u.getId(), Integer.parseInt(req.getParameter("id")), nuevoRol);
                    sesion.setAttribute("flashOk", "Rol actualizado a " + nuevoRol);
                }
                case "cambiarActivoUsuario" -> {
                    exigir(u, Constantes.ROL_ADMINISTRADOR);
                    boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                    usuarioService.cambiarActivo(u.getId(), Integer.parseInt(req.getParameter("id")), activo);
                    sesion.setAttribute("flashOk", activo ? "Usuario activado" : "Usuario desactivado");
                }
                case "resetearContrasena" -> {
                    exigir(u, Constantes.ROL_ADMINISTRADOR);
                    usuarioService.resetearContrasena(Integer.parseInt(req.getParameter("id")),
                            req.getParameter("contrasena"));
                    sesion.setAttribute("flashOk", "Contraseña restablecida");
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

    /** El historial vive en la sesión: cada persona deshace solo sus propios cambios. */
    private HistorialComandos historialReclamos(HttpSession sesion) {
        HistorialComandos historial = (HistorialComandos) sesion.getAttribute("historialReclamos");
        if (historial == null) {
            historial = new HistorialComandos();
            sesion.setAttribute("historialReclamos", historial);
        }
        return historial;
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
