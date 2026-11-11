package com.botica.service;

import com.botica.dao.factory.DAOFactory;
import com.botica.model.DetallePedido;
import com.botica.model.Pago;
import com.botica.model.Pedido;
import com.botica.model.Producto;
import com.botica.model.Reclamo;
import com.botica.model.Usuario;
import com.botica.patterns.memento.HistorialPedidos;
import java.util.List;

/** Consultas de solo lectura para las vistas: así los JSP ya no tocan la capa DAO directamente. */
public final class ConsultaService {

    private ConsultaService() {
    }

    public static List<Producto> productos() {
        return DAOFactory.getProductoDAO().listarTodos();
    }

    public static Producto productoPorId(int id) {
        return DAOFactory.getProductoDAO().buscarPorId(id);
    }

    public static List<Pedido> pedidos() {
        return DAOFactory.getPedidoDAO().listarTodos();
    }

    public static Pedido pedidoPorId(int id) {
        return DAOFactory.getPedidoDAO().buscarPorId(id);
    }

    public static boolean puedeRevertir(int idPedido) {
        return HistorialPedidos.getInstancia().ultima(idPedido) != null;
    }

    public static List<Pedido> pedidosDeCliente(int idCliente) {
        return DAOFactory.getPedidoDAO().listarPorCliente(idCliente);
    }

    public static List<DetallePedido> detallesDePedido(int idPedido) {
        return DAOFactory.getDetallePedidoDAO().listarPorPedido(idPedido);
    }

    public static List<Pago> pagosDePedido(int idPedido) {
        return DAOFactory.getPagoDAO().listarPorPedido(idPedido);
    }

    public static List<Reclamo> reclamos() {
        return DAOFactory.getReclamoDAO().listarTodos();
    }

    public static List<Reclamo> reclamosDeCliente(int idCliente) {
        return DAOFactory.getReclamoDAO().listarPorCliente(idCliente);
    }

    public static List<Usuario> usuarios() {
        return DAOFactory.getUsuarioDAO().listarTodos();
    }
}
