package com.botica.patterns.pago;

import com.botica.util.ReglaNegocioException;
import java.util.List;

/** Patrón Factory (Creacional): entrega la estrategia de pago que corresponde al código elegido. */
public final class MetodoPagoFactory {
    private static final List<MetodoPago> METODOS =
            List.of(new PagoTarjeta(), new PagoYape(), new PagoMercadoPago());

    private MetodoPagoFactory() {
    }

    public static MetodoPago desde(String codigo) {
        for (MetodoPago metodo : METODOS) {
            if (metodo.codigo().equals(codigo)) {
                return metodo;
            }
        }
        throw new ReglaNegocioException("Elige un método de pago válido");
    }

    /** Métodos que se ofrecen en la pantalla de pago. */
    public static List<MetodoPago> disponibles() {
        return METODOS;
    }
}
