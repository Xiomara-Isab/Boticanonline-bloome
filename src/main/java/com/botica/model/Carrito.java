package com.botica.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito de compras del cliente. Vive solo en la sesión HTTP (no hay tabla): si el cliente cierra
 * sesión, el carrito se pierde. Guarda idProducto -> cantidad, respetando el orden en que se agregó.
 */
public class Carrito implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<Integer, Integer> lineas = new LinkedHashMap<>();

    public void agregar(int idProducto, int cantidad) {
        lineas.merge(idProducto, cantidad, Integer::sum);
    }

    public void fijarCantidad(int idProducto, int cantidad) {
        if (lineas.containsKey(idProducto)) {
            lineas.put(idProducto, cantidad);
        }
    }

    public void quitar(int idProducto) {
        lineas.remove(idProducto);
    }

    public void vaciar() {
        lineas.clear();
    }

    public int cantidadDe(int idProducto) {
        return lineas.getOrDefault(idProducto, 0);
    }

    /** Suma de unidades de todas las líneas (el número que se muestra en la barra). */
    public int getCantidadTotal() {
        int total = 0;
        for (int cantidad : lineas.values()) {
            total += cantidad;
        }
        return total;
    }

    public boolean isVacio() {
        return lineas.isEmpty();
    }

    public Map<Integer, Integer> getLineas() {
        return Collections.unmodifiableMap(lineas);
    }
}
