package com.botica.patterns.memento;

import com.botica.model.PedidoMemento;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Cuidador del patrón Memento: guarda las fotos de cada pedido sin mirar su contenido.
 * Es compartido porque el pedido también lo es: la última foto siempre corresponde al último avance.
 * shortcut: vive en memoria y se pierde al reiniciar Tomcat; pasar a una tabla si debe sobrevivir al reinicio.
 */
public class HistorialPedidos {
    private static final HistorialPedidos INSTANCIA = new HistorialPedidos();

    private final Map<Integer, Deque<PedidoMemento>> fotos = new HashMap<>();

    public static HistorialPedidos getInstancia() {
        return INSTANCIA;
    }

    public synchronized void guardar(int idPedido, PedidoMemento foto) {
        fotos.computeIfAbsent(idPedido, id -> new ArrayDeque<>()).push(foto);
    }

    /** Última foto del pedido, o null si no hay ninguna. */
    public synchronized PedidoMemento ultima(int idPedido) {
        Deque<PedidoMemento> pila = fotos.get(idPedido);
        return pila == null ? null : pila.peek();
    }

    public synchronized void descartarUltima(int idPedido) {
        Deque<PedidoMemento> pila = fotos.get(idPedido);
        pila.pop();
        if (pila.isEmpty()) {
            fotos.remove(idPedido);
        }
    }
}
