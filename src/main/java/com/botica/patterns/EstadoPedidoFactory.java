package com.botica.patterns;

import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import java.util.Map;

/** Devuelve el objeto State correspondiente al nombre del estado (sin if/switch en el servicio). */
public final class EstadoPedidoFactory {
    private static final Map<String, EstadoPedido> ESTADOS = Map.of(
            Constantes.PENDIENTE, new EstadoPendiente(),
            Constantes.PAGADO, new EstadoPagado(),
            Constantes.EN_DESPACHO, new EstadoEnDespacho(),
            Constantes.ENVIADO, new EstadoEnviado(),
            Constantes.ENTREGADO, new EstadoEntregado(),
            Constantes.CANCELADO, new EstadoCancelado());

    private EstadoPedidoFactory() {
    }

    public static EstadoPedido desde(String nombre) {
        EstadoPedido estado = ESTADOS.get(nombre);
        if (estado == null) {
            throw new ReglaNegocioException("Estado de pedido desconocido: " + nombre);
        }
        return estado;
    }
}
