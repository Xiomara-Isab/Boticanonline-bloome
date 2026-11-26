package com.botica.controller;

import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.patterns.command.Comando;
import com.botica.patterns.command.HistorialComandos;
import com.botica.patterns.facade.PedidoFacade;
import com.botica.patterns.proxy.ProductoServiceProxy;
import com.botica.service.AuditoriaService;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/**
 * GRASP Controlador del panel por rol: GET muestra la vista del rol; POST valida el permiso del actor
 * y delega la regla de negocio en el servicio correspondiente. Las acciones del catálogo pasan por
 * ProductoServiceProxy, que revisa el rol antes de llegar al servicio real. El resto de permisos por
 * acción están en el mapa de RolFilter.
 */
@WebServlet("/panel")
public class PanelController extends HttpServlet {
    private final ProductoService productoServiceReal = new ProductoServiceImpl();
    private final UsuarioService usuarioService = new UsuarioService();
    private final ReclamoService reclamoService = new ReclamoService();
    private final PedidoFacade pedidoFacade = new PedidoFacade();
    private final AuditoriaService auditoria = new AuditoriaService();

    /** Acciones del panel que quedan en la bitácora y la entidad que tocan (reembolsos: NotificadorAuditoria). */
    private static final Map<String, String> AUDITADAS = Map.ofEntries(
            Map.entry("crearProducto", "PRODUCTO"), Map.entry("actualizarProducto", "PRODUCTO"),
            Map.entry("ajustarStock", "PRODUCTO"), Map.entry("eliminarProducto", "PRODUCTO"),
            Map.entry("cambiarActivoProducto", "PRODUCTO"), Map.entry("aplicarDescuento", "PRODUCTO"),
            Map.entry("registrarIngreso", "PRODUCTO"), Map.entry("retirarVencidos", "PRODUCTO"),
            Map.entry("cambiarRol", "USUARIO"), Map.entry("cambiarActivoUsuario", "USUARIO"),
            Map.entry("crearUsuario", "USUARIO"));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = usuarioActual(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        if ("exportarCsv".equals(req.getParameter("accion"))) { // solo Auditor (RolFilter)
            exportarCsv(req, resp);
            return;
        }
        String vista = switch (u.getRol()) {
            case Constantes.ROL_VENDEDOR -> "vendedor";
            case Constantes.ROL_ALMACEN -> "almacen";
            case Constantes.ROL_SOPORTE -> "soporte";
            case Constantes.ROL_ADMINISTRADOR -> "admin";
            case Constantes.ROL_AUDITOR -> "auditor";
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
                    pedidoFacade.registrarReclamo(u.getId(), Integer.parseInt(req.getParameter("idPedido")),
                            req.getParameter("descripcion"));
                    sesion.setAttribute("flashOk", "Reclamo enviado. Soporte te atenderá pronto");
                }
                case "estadoReclamo" -> {
                    reclamoService.cambiarEstado(Integer.parseInt(req.getParameter("id")), req.getParameter("estado"),
                            historialReclamos(sesion));
                    sesion.setAttribute("flashOk", "Reclamo actualizado");
                }
                case "responderReclamo" -> {
                    reclamoService.responder(u.getId(), Integer.parseInt(req.getParameter("id")),
                            req.getParameter("respuesta"));
                    sesion.setAttribute("flashOk", "Respuesta enviada al cliente");
                }
                case "reembolsarPedido" -> {
                    var reembolso = pedidoFacade.reembolsarPedido(Integer.parseInt(req.getParameter("idPedido")));
                    sesion.setAttribute("flashOk", "Pedido #" + reembolso.getIdPedido() + " reembolsado ("
                            + com.botica.util.Html.money(-reembolso.getMonto()) + "). El stock volvió al inventario");
                }
                case "deshacerReclamo" -> {
                    Comando deshecho = historialReclamos(sesion).deshacer();
                    sesion.setAttribute("flashOk", "Deshecho: " + deshecho.descripcion());
                }
                case "cambiarRol" -> {
                    String nuevoRol = req.getParameter("rol");
                    usuarioService.cambiarRol(u.getId(), Integer.parseInt(req.getParameter("id")), nuevoRol);
                    sesion.setAttribute("flashOk", "Rol actualizado a " + nuevoRol);
                }
                case "cambiarActivoUsuario" -> {
                    boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                    usuarioService.cambiarActivo(u.getId(), Integer.parseInt(req.getParameter("id")), activo);
                    sesion.setAttribute("flashOk", activo ? "Usuario activado" : "Usuario desactivado");
                }
                case "crearUsuario" -> {
                    String rol = req.getParameter("rol");
                    usuarioService.crearPersonal(req.getParameter("nombre"), req.getParameter("correo"),
                            req.getParameter("contrasena"), rol);
                    sesion.setAttribute("flashOk", "Usuario creado con rol " + rol);
                }
                default -> sesion.setAttribute("flashErr", "Acción no reconocida");
            }
            if (AUDITADAS.containsKey(accion)) {
                auditoria.registrar(accion, AUDITADAS.get(accion), idOpcional(req.getParameter("id")),
                        (String) sesion.getAttribute("flashOk"));
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

    private LocalDate fechaOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : LocalDate.parse(valor);
    }

    private Integer idOpcional(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Descarga la bitácora con los mismos filtros que la pantalla del auditor. */
    private void exportarCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalDate desde, hasta;
        try {
            desde = fechaOpcional(req.getParameter("desde"));
            hasta = fechaOpcional(req.getParameter("hasta"));
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fecha no válida");
            return;
        }
        // shortcut: tope de 100 000 filas en memoria; paginar o hacer streaming si la bitácora crece mucho
        String csv = AuditoriaService.csv(auditoria.listar(desde, hasta, req.getParameter("usuario"),
                req.getParameter("filtroAccion"), 100_000));
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"auditoria-" + LocalDate.now() + ".csv\"");
        resp.getOutputStream().write(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8)); // BOM para que Excel lea tildes
    }
}
