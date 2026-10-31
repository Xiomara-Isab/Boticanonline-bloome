package com.botica.patterns;

import com.botica.model.Pedido;
import com.botica.util.Constantes;

public class EstadoEnDespacho implements EstadoPedido {
    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(Constantes.ENVIADO);
    }

    @Override
    public String getNombreEstado() {
        return Constantes.EN_DESPACHO;
    }
}
