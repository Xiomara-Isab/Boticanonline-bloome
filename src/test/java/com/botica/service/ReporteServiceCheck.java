package com.botica.service;

import com.botica.dao.ReporteDAO;
import com.botica.model.ProductoVendido;
import com.botica.model.VentaDia;
import com.botica.util.ReglaNegocioException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprobación sin base de datos de ReporteService: suma de totales, rango del día y validación de fechas.
 * Qué pedidos cuentan como venta lo decide la consulta SQL de ReporteDAOImpl (se prueba a mano contra la base).
 * Ejecutar con: java -ea -cp "target/classes:target/test-classes" com.botica.service.ReporteServiceCheck
 */
public class ReporteServiceCheck {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 25);
    private static final List<VentaDia> BD = new ArrayList<>();
    private static LocalDate pidioDesde, pidioHasta;

    private static final ReporteDAO DAO = new ReporteDAO() {
        @Override public List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta) {
            pidioDesde = desde;
            pidioHasta = hasta;
            List<VentaDia> filtradas = new ArrayList<>();
            for (VentaDia d : BD) if (!d.fecha().isBefore(desde) && !d.fecha().isAfter(hasta)) filtradas.add(d);
            return filtradas;
        }
        @Override public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite) {
            return List.of(new ProductoVendido(1, "Paracetamol", 5, 42.5));
        }
    };

    public static void main(String[] args) {
        BD.add(new VentaDia(HOY.minusDays(2), 1, 33.40));
        BD.add(new VentaDia(HOY.minusDays(1), 2, 48.10));
        BD.add(new VentaDia(HOY, 3, 45.20));
        ReporteService servicio = new ReporteService(DAO, () -> HOY);

        // ventas del día: pide exactamente hoy-hoy y devuelve solo lo de hoy
        ReporteService.ResumenVentas hoy = servicio.ventasDelDia();
        assert pidioDesde.equals(HOY) && pidioHasta.equals(HOY);
        assert hoy.pedidos() == 3 && hoy.total() == 45.20 : hoy;

        // rango: suma pedidos y totales de todos los días, redondeado a céntimos (33.40 + 48.10 + 45.20)
        ReporteService.ResumenVentas rango = servicio.ventasPorRango(HOY.minusDays(2), HOY);
        assert rango.dias().size() == 3;
        assert rango.pedidos() == 6;
        assert rango.total() == 126.70 : rango.total();

        // un rango sin ventas da cero, no error
        ReporteService.ResumenVentas vacio = servicio.ventasPorRango(HOY.plusDays(1), HOY.plusDays(5));
        assert vacio.pedidos() == 0 && vacio.total() == 0 && vacio.dias().isEmpty();

        // fechas inválidas
        rechaza(() -> servicio.ventasPorRango(HOY, HOY.minusDays(1)));
        rechaza(() -> servicio.ventasPorRango(null, HOY));
        rechaza(() -> servicio.ventasPorRango(HOY.minusDays(ReporteService.DIAS_MAXIMOS), HOY));
        rechaza(() -> servicio.masVendidos(HOY, HOY.minusDays(1)));
        assert servicio.masVendidos(HOY.minusDays(7), HOY).get(0).unidades() == 5;
        System.out.println("OK");
    }

    private static void rechaza(Runnable accion) {
        try {
            accion.run();
            assert false : "debía rechazarse";
        } catch (ReglaNegocioException esperado) {
            // correcto
        }
    }
}
