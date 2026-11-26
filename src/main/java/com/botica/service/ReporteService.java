package com.botica.service;

import com.botica.dao.ReporteDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.ProductoVendido;
import com.botica.model.VentaDia;
import com.botica.util.ReglaNegocioException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;

/**
 * GRASP Experto en información: los totales de ventas los calcula este servicio (con su DAO), que es quien
 * tiene los datos de pedidos y pagos; los JSP solo muestran el resultado. Qué cuenta como venta: ver ReporteDAOImpl.
 */
public class ReporteService {
    public static final int DIAS_MAXIMOS = 366;
    public static final int TOP_PRODUCTOS = 10;

    /** Ventas de un rango: el detalle por día y los totales ya sumados. */
    public record ResumenVentas(LocalDate desde, LocalDate hasta, List<VentaDia> dias, int pedidos, double total) {
    }

    private final ReporteDAO reporteDAO;
    private final Supplier<LocalDate> hoy;

    public ReporteService() {
        this(DAOFactory.getReporteDAO(), LocalDate::now);
    }

    public ReporteService(ReporteDAO reporteDAO, Supplier<LocalDate> hoy) {
        this.reporteDAO = reporteDAO;
        this.hoy = hoy;
    }

    public ResumenVentas ventasDelDia() {
        LocalDate dia = hoy.get();
        return ventasPorRango(dia, dia);
    }

    public ResumenVentas ventasPorRango(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<VentaDia> dias = reporteDAO.ventasPorDia(desde, hasta);
        int pedidos = 0;
        double total = 0;
        for (VentaDia d : dias) {
            pedidos += d.pedidos();
            total += d.total();
        }
        return new ResumenVentas(desde, hasta, dias, pedidos, Math.round(total * 100) / 100.0);
    }

    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        return reporteDAO.masVendidos(desde, hasta, TOP_PRODUCTOS);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Indica la fecha de inicio y de fin");
        }
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) >= DIAS_MAXIMOS) {
            throw new ReglaNegocioException("El rango puede ser de hasta " + DIAS_MAXIMOS + " días");
        }
    }
}
