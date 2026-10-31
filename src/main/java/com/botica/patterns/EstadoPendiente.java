package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;

public class EstadoPendiente implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(Constantes.PAGADO);
    }

    @Override
    public String getNombreEstado() {
        return Constantes.PENDIENTE;
    }
}
