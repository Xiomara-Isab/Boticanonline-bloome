package com.botica.model;

/**
 * Patrón Memento (Comportamiento): foto inmutable del estado de un pedido.
 * Está en este paquete para que solo Pedido pueda leerla; quien la guarda no ve su contenido.
 */
public final class PedidoMemento {
    final String estado;
    final double total;

    PedidoMemento(String estado, double total) {
        this.estado = estado;
        this.total = total;
    }
}
