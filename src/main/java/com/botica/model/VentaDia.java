package com.botica.model;

import java.time.LocalDate;

/** Una fila del reporte de ventas: lo vendido en un día (fecha del pago). */
public record VentaDia(LocalDate fecha, int pedidos, double total) {
}
