package com.botica.service;

import com.botica.dao.PedidoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Pedido;
import com.botica.patterns.EstadoPedido;
import com.botica.patterns.EstadoPedidoFactory;
import com.botica.patterns.PedidoSujeto;
import com.botica.util.ReglaNegocioException;

/** Reglas de negocio del ciclo de vida del pedido (usa State para avanzar y Observer para avisar). */
public class PedidoService {
    private final PedidoDAO pedidoDAO;
    private final PedidoSujeto notificador;

    public PedidoService() {
        this(DAOFactory.getPedidoDAO(), PedidoSujeto.crearPorDefecto());
    }

    public PedidoService(PedidoDAO pedidoDAO, PedidoSujeto notificador) {
        this.pedidoDAO = pedidoDAO;
        this.notificador = notificador;
    }

    public Pedido avanzarEstado(int idPedido) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null) {
            throw new ReglaNegocioException("El pedido no existe");
        }
        EstadoPedido estadoActual = EstadoPedidoFactory.desde(pedido.getEstado());
        if (estadoActual.esFinal()) {
            throw new ReglaNegocioException("El pedido #" + idPedido + " ya fue entregado");
        }
        estadoActual.avanzar(pedido);
        pedidoDAO.actualizar(pedido);
        notificador.notificar("Tu pedido #" + pedido.getId() + " cambió a " + pedido.getEstado());
        return pedido;
    }
}
