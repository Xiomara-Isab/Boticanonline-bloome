package com.botica.controller;

import com.botica.model.Pago;
import com.botica.model.Pedido;
import com.botica.model.Usuario;
import com.botica.patterns.facade.ItemCarrito;
import com.botica.patterns.facade.PedidoFacade;
import com.botica.util.Constantes;
import com.botica.util.Html;
import com.botica.util.ReglaNegocioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/** GRASP Controlador: coordina compras, pagos, cancelaciones, avance y reversión de pedidos a través del Facade. */
@WebServlet({"/comprar", "/pagar", "/cancelarPedido", "/avanzarPedido", "/revertirPedido"})
public class PedidoController extends HttpServlet {
    private final PedidoFacade pedidoFacade = new PedidoFacade();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        switch (req.getServletPath()) {
            case "/comprar" -> comprar(req, resp);
            case "/pagar" -> pagar(req, resp);
            case "/cancelarPedido" -> cancelar(req, resp);
            default -> avanzarPedido(req, resp);
        }
    }

    private void comprar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?compra=1");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (!Constantes.ROL_CLIENTE.equals(usuario.getRol())) {
            req.setAttribute("error", "Solo las cuentas de cliente pueden realizar compras");
            req.getRequestDispatcher("catalogo.jsp").forward(req, resp);
            return;
        }

        try {
            int idProducto = Integer.parseInt(req.getParameter("idProducto"));
            int cantidad = Integer.parseInt(req.getParameter("cantidad"));
            Pedido pedido = pedidoFacade.realizarPedido(usuario.getId(), List.of(new ItemCarrito(idProducto, cantidad)));
            // El pedido nace PENDIENTE: el cliente continúa en la pantalla de pago.
            resp.sendRedirect(req.getContextPath() + "/pago.jsp?id=" + pedido.getId());
        } catch (NumberFormatException e) {
            reenviarCatalogo(req, resp, "Datos de compra inválidos");
        } catch (ReglaNegocioException e) {
            reenviarCatalogo(req, resp, e.getMessage());
        } catch (RuntimeException e) {
            reenviarCatalogo(req, resp, "No se pudo completar el pedido. Inténtalo nuevamente");
        }
    }

    /** Pago simulado: el cliente elige un método y el pedido pasa de PENDIENTE a PAGADO. */
    private void pagar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?sesion=1");
            return;
        }
        if (!Constantes.ROL_CLIENTE.equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idTexto = req.getParameter("idPedido");
        try {
            int idPedido = Integer.parseInt(idTexto);
            Pago pago = pedidoFacade.pagarPedido(usuario.getId(), idPedido, req.getParameter("metodo"),
                    req.getParameter("dato"));
            sesion.setAttribute("flashOk", "Pago aprobado. Código de operación: " + pago.getReferencia());
            resp.sendRedirect(req.getContextPath() + "/seguimiento.jsp?id=" + idPedido);
        } catch (NumberFormatException e) {
            sesion.setAttribute("flashErr", "Pedido no válido");
            resp.sendRedirect(req.getContextPath() + "/panel");
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/pago.jsp?id=" + idTexto);
        } catch (RuntimeException e) {
            sesion.setAttribute("flashErr", "No se pudo procesar el pago. Inténtalo nuevamente");
            resp.sendRedirect(req.getContextPath() + "/pago.jsp?id=" + idTexto);
        }
    }

    /** El cliente cancela un pedido que todavía no pagó. */
    private void cancelar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?sesion=1");
            return;
        }
        if (!Constantes.ROL_CLIENTE.equals(usuario.getRol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            int idPedido = Integer.parseInt(req.getParameter("idPedido"));
            pedidoFacade.cancelarPedido(usuario.getId(), idPedido);
            sesion.setAttribute("flashOk", "Pedido #" + idPedido + " cancelado. El stock volvió al inventario");
        } catch (NumberFormatException e) {
            sesion.setAttribute("flashErr", "Pedido no válido");
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
        } catch (RuntimeException e) {
            sesion.setAttribute("flashErr", "No se pudo cancelar el pedido. Inténtalo nuevamente");
        }
        resp.sendRedirect(req.getContextPath() + "/panel");
    }

    private void avanzarPedido(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        String rol = usuario.getRol();
        if (!Constantes.ROL_ALMACEN.equals(rol) && !Constantes.ROL_ADMINISTRADOR.equals(rol)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String idTexto = req.getParameter("idPedido");
        try {
            int idPedido = Integer.parseInt(idTexto);
            if ("/revertirPedido".equals(req.getServletPath())) {
                Pedido pedido = pedidoFacade.revertirPedido(idPedido);
                sesion.setAttribute("flashOk", "Pedido #" + idPedido + " revertido a " + Html.estadoLegible(pedido.getEstado()));
            } else {
                pedidoFacade.avanzarPedido(idPedido);
            }
        } catch (NumberFormatException e) {
            sesion.setAttribute("flashErr", "Pedido no válido");
        } catch (ReglaNegocioException e) {
            sesion.setAttribute("flashErr", e.getMessage());
        }

        if ("panel".equals(req.getParameter("volver")) || idTexto == null) {
            resp.sendRedirect(req.getContextPath() + "/panel");
        } else {
            resp.sendRedirect(req.getContextPath() + "/seguimiento.jsp?id=" + idTexto);
        }
    }

    private void reenviarCatalogo(HttpServletRequest req, HttpServletResponse resp, String mensaje)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.getRequestDispatcher("catalogo.jsp").forward(req, resp);
    }
}
