package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/** Estado final: el cliente canceló el pedido antes de pagarlo. */
public class EstadoCancelado implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        throw new ReglaNegocioException("El pedido #" + pedido.getId() + " fue cancelado");
    }

    @Override
    public String getNombreEstado() {
        return Constantes.CANCELADO;
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
