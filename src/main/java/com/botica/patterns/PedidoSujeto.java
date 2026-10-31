package com.botica.patterns;

import java.util.ArrayList;
import java.util.List;

/** Patrón Observer (Comportamiento): sujeto que avisa a todos sus observadores cuando cambia un pedido. */
public class PedidoSujeto {
    private final List<NotificadorPedido> observadores = new ArrayList<>();

    public void agregarObservador(NotificadorPedido o) {
        observadores.add(o);
    }

    public void quitarObservador(NotificadorPedido o) {
        observadores.remove(o);
    }

    public void notificar(String mensaje) {
        for (NotificadorPedido o : observadores) {
            o.notificar(mensaje);
        }
    }

    /** Sujeto con los observadores estándar del sistema. */
    public static PedidoSujeto crearPorDefecto() {
        PedidoSujeto s = new PedidoSujeto();
        s.agregarObservador(new NotificadorCliente());
        s.agregarObservador(new NotificadorAlmacen());
        return s;
    }
}
