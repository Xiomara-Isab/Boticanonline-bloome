package com.botica.patterns;

public class NotificadorCliente implements NotificadorPedido {
    @Override
    public void notificar(String mensaje) {
        System.out.println("Notificación al cliente: " + mensaje);
    }
}
