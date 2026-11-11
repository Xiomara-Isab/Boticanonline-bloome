package com.botica.controller;

import com.botica.model.Carrito;
import com.botica.model.Pedido;
import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.patterns.facade.PedidoFacade;
import com.botica.service.CarritoService;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** GRASP Controlador del carrito: agregar, cambiar cantidad, quitar, vaciar y finalizar la compra. */
@WebServlet("/carrito")
public class CarritoController extends HttpServlet {
    private final CarritoService carritoService = new CarritoService();
    private final PedidoFacade pedidoFacade = new PedidoFacade();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(ctx + "/login.jsp?compra=1");
            return;
        }
        if (!Constantes.ROL_CLIENTE.equals(usuario.getRol())) {
            sesion.setAttribute("flashErr", "Solo las cuentas de cliente pueden usar el carrito");
            resp.sendRedirect(ctx + "/panel");
            return;
        }

        Carrito carrito = (Carrito) sesion.getAttribute("carrito");
        if (carrito == null) {
            carrito = new Carrito();
            sesion.setAttribute("carrito", carrito);
        }

        String accion = req.getParameter("accion") == null ? "" : req.getParameter("accion");
        String destino = "/carrito.jsp";
        try {
            switch (accion) {
                case "agregar" -> {
                    Producto producto = carritoService.agregar(carrito, Integer.parseInt(req.getParameter("idProducto")),
                            Integer.parseInt(req.getParameter("cantidad")));
                    sesion.setAttribute("flashOk", producto.getNombre() + " se agregó al carrito");
                    destino = volverA(req.getParameter("volver"));
                }
                case "cambiar" -> {
                    carritoService.cambiarCantidad(carrito, Integer.parseInt(req.getParameter("idProducto")),
                            Integer.parseInt(req.getParameter("cantidad")));
                    sesion.setAttribute("flashOk", "Carrito actualizado");
                }
                case "quitar" -> {
                    carritoService.quitar(carrito, Integer.parseInt(req.getParameter("idProducto")));
                    sesion.setAttribute("flashOk", "Producto quitado del carrito");
                }
                case "vaciar" -> {
                    carrito.vaciar();
                    sesion.setAttribute("flashOk", "Carrito vaciado");
                }
                case "finalizar" -> {
                    if (carrito.isVacio()) {
                        throw new ReglaNegocioException("Tu carrito está vacío");
                    }
                    int unidades = carrito.getCantidadTotal();
                    // Un solo pedido con todos los productos; el Facade valida stock, productos activos y descuentos.
                    Pedido pedido = pedidoFacade.realizarPedido(usuario.getId(), carritoService.items(carrito));
                    carrito.vaciar();
                    sesion.setAttribute("flashOk", "Pedido #" + pedido.getId() + " creado con " + unidades
                            + " unidad(es). Elige cómo pagar");
                    destino = "/pago.jsp?id=" + pedido.getId();
                }
                default -> sesion.setAttribute("flashErr", "Acción no válida");
            }
        } catch (NumberFormatException e) {
            sesion.setAttribute("flashErr", "Datos no válidos");
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
            if ("agregar".equals(accion)) {
                destino = volverA(req.getParameter("volver"));
            }
        } catch (RuntimeException e) {
            sesion.setAttribute("flashErr", "No se pudo completar la acción. Inténtalo nuevamente");
        }
        resp.sendRedirect(ctx + destino);
    }

    /** Solo se vuelve al catálogo (con su búsqueda); cualquier otro valor se ignora para evitar redirecciones raras. */
    private String volverA(String volver) {
        if (volver != null && volver.matches("catalogo\\.jsp(\\?[\\w=&%+.\\-]*)?")) {
            return "/" + volver;
        }
        return "/carrito.jsp";
    }
}
