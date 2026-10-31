package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;

public class EstadoPagado implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(Constantes.EN_DESPACHO);
    }

    @Override
    public String getNombreEstado() {
        return Constantes.PAGADO;
    }
}
