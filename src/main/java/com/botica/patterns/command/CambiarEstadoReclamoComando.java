package com.botica.patterns.command;

import com.botica.dao.ReclamoDAO;
import com.botica.model.Reclamo;
import com.botica.util.Html;
import com.botica.util.ReglaNegocioException;

/** Comando concreto: cambia el estado de un reclamo y guarda el anterior para poder revertirlo. */
public class CambiarEstadoReclamoComando implements Comando {
    private final ReclamoDAO reclamoDAO;
    private final int idReclamo;
    private final String estadoAnterior;
    private final String estadoNuevo;

    public CambiarEstadoReclamoComando(ReclamoDAO reclamoDAO, int idReclamo, String estadoAnterior, String estadoNuevo) {
        this.reclamoDAO = reclamoDAO;
        this.idReclamo = idReclamo;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }

    @Override
    public void ejecutar() {
        reclamoDAO.actualizarEstado(idReclamo, estadoNuevo);
    }

    /**
     * Si otro usuario cambió el reclamo después, no se revierte para no pisar su trabajo.
     * shortcut: leer y actualizar no es atómico, usar UPDATE ... WHERE estado = ? si dos personas chocan a menudo.
     */
    @Override
    public void deshacer() {
        Reclamo actual = reclamoDAO.buscarPorId(idReclamo);
        if (actual == null || !estadoNuevo.equals(actual.getEstado())) {
            throw new ReglaNegocioException("No se puede deshacer: el reclamo #" + idReclamo + " fue modificado por otro usuario");
        }
        reclamoDAO.actualizarEstado(idReclamo, estadoAnterior);
    }

    @Override
    public String descripcion() {
        return "reclamo #" + idReclamo + " de " + Html.estadoLegible(estadoAnterior) + " a " + Html.estadoLegible(estadoNuevo);
    }
}
