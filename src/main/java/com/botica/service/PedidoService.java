package com.botica.service;

import com.botica.dao.PedidoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Pedido;
import com.botica.model.PedidoMemento;
import com.botica.patterns.EstadoPedido;
import com.botica.patterns.EstadoPedidoFactory;
import com.botica.patterns.PedidoSujeto;
import com.botica.patterns.memento.HistorialPedidos;
import com.botica.util.ReglaNegocioException;

/** Reglas de negocio del ciclo de vida del pedido (usa State para avanzar, Memento para revertir y Observer para avisar). */
public class PedidoService {
    private final PedidoDAO pedidoDAO;
    private final PedidoSujeto notificador;
    private final HistorialPedidos historial;

    public PedidoService() {
        this(DAOFactory.getPedidoDAO(), PedidoSujeto.crearPorDefecto(), HistorialPedidos.getInstancia());
    }

    public PedidoService(PedidoDAO pedidoDAO, PedidoSujeto notificador, HistorialPedidos historial) {
        this.pedidoDAO = pedidoDAO;
        this.notificador = notificador;
        this.historial = historial;
    }

    public Pedido avanzarEstado(int idPedido) {
        // el bloqueo compartido evita que un avance y una reversión simultáneos desordenen las fotos
        synchronized (historial) {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                throw new ReglaNegocioException("El pedido no existe");
            }
            EstadoPedido estadoActual = EstadoPedidoFactory.desde(pedido.getEstado());
            if (estadoActual.esFinal()) {
                throw new ReglaNegocioException("El pedido #" + idPedido + " ya fue entregado");
            }
            PedidoMemento foto = pedido.crearMemento();
            estadoActual.avanzar(pedido);
            pedidoDAO.actualizar(pedido);
            historial.guardar(idPedido, foto);
            notificador.notificar("Tu pedido #" + pedido.getId() + " cambió a " + pedido.getEstado());
            return pedido;
        }
    }

    /** Devuelve el pedido a su última foto. Si falla la BD, la foto se conserva para reintentar. */
    public Pedido revertirEstado(int idPedido) {
        synchronized (historial) {
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                throw new ReglaNegocioException("El pedido no existe");
            }
            PedidoMemento foto = historial.ultima(idPedido);
            if (foto == null) {
                throw new ReglaNegocioException("El pedido #" + idPedido + " no tiene cambios para revertir");
            }
            pedido.restaurar(foto);
            pedidoDAO.actualizar(pedido);
            historial.descartarUltima(idPedido);
            notificador.notificar("Tu pedido #" + pedido.getId() + " volvió a " + pedido.getEstado());
            return pedido;
        }
    }
}
