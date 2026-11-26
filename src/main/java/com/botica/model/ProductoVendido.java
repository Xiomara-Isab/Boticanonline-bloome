package com.botica.model;

/** Una fila del reporte de productos más vendidos. */
public record ProductoVendido(int idProducto, String nombre, int unidades, double total) {
}
