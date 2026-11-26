package com.botica.dao;

import com.botica.model.ProductoVendido;
import com.botica.model.VentaDia;
import java.time.LocalDate;
import java.util.List;

/** Consultas de ventas. Los rangos de fechas son inclusivos en ambos extremos. */
public interface ReporteDAO {
    List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta);
    List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite);
}
