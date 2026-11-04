package com.botica.service;

import com.botica.dao.PedidoDAO;
import com.botica.dao.ReclamoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Pedido;
import com.botica.model.Reclamo;
import com.botica.patterns.command.CambiarEstadoReclamoComando;
import com.botica.patterns.command.HistorialComandos;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/** Reglas de negocio de reclamos y devoluciones. */
public class ReclamoService {
    private final ReclamoDAO reclamoDAO;
    private final PedidoDAO pedidoDAO;

    public ReclamoService() {
        this(DAOFactory.getReclamoDAO(), DAOFactory.getPedidoDAO());
    }

    public ReclamoService(ReclamoDAO reclamoDAO, PedidoDAO pedidoDAO) {
        this.reclamoDAO = reclamoDAO;
        this.pedidoDAO = pedidoDAO;
    }

    public void crear(int idCliente, int idPedido, String descripcion) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null || pedido.getIdCliente() != idCliente) {
            throw new ReglaNegocioException("Pedido no válido");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException("Describe el problema para poder ayudarte");
        }
        reclamoDAO.crear(new Reclamo(idPedido, descripcion.trim(), Constantes.RECLAMO_ABIERTO));
    }

    /** Cambia el estado mediante un comando guardado en el historial, para poder deshacerlo. */
    public void cambiarEstado(int idReclamo, String estado, HistorialComandos historial) {
        if (!Constantes.ESTADOS_RECLAMO.contains(estado)) {
            throw new ReglaNegocioException("Estado de reclamo inválido");
        }
        Reclamo reclamo = reclamoDAO.buscarPorId(idReclamo);
        if (reclamo == null) {
            throw new ReglaNegocioException("El reclamo no existe");
        }
        if (!estado.equals(reclamo.getEstado())) {
            historial.ejecutar(new CambiarEstadoReclamoComando(reclamoDAO, idReclamo, reclamo.getEstado(), estado));
        }
    }
}
