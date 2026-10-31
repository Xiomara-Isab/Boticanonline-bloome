package com.botica.patterns;

import com.botica.model.Pedido;

/** Patrón State (Comportamiento): cada estado del pedido sabe a cuál pasa y si es el último. */
public interface EstadoPedido {
    void avanzar(Pedido pedido);

    String getNombreEstado();

    /** Un estado final no admite más transiciones. */
    default boolean esFinal() {
        return false;
    }
}
