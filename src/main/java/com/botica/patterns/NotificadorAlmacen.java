package com.botica.patterns;

/** Observador concreto: deja registro para el personal de almacén. */
public class NotificadorAlmacen implements NotificadorPedido {
    @Override
    public void notificar(String mensaje) {
        System.out.println("Aviso a almacén: " + mensaje);
    }
}
