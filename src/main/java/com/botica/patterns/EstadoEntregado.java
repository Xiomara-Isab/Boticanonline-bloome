package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;

public class EstadoEntregado implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        // Estado final: no hay transición posterior.
    }

    @Override
    public String getNombreEstado() {
        return Constantes.ENTREGADO;
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
