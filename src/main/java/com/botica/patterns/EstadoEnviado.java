package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;

public class EstadoEnviado implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(Constantes.ENTREGADO);
    }

    @Override
    public String getNombreEstado() {
        return Constantes.ENVIADO;
    }
}
