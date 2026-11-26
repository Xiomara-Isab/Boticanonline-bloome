package com.botica.patterns.pago;

import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/** Estrategia: billetera digital Yape / Plin (simulada). */
public class PagoYape implements MetodoPago {
    @Override
    public String codigo() {
        return Constantes.PAGO_YAPE;
    }

    @Override
    public String etiqueta() {
        return "Yape / Plin";
    }

    @Override
    public String ayuda() {
        return "Número de celular (9 dígitos, empieza con 9).";
    }

    @Override
    public String procesar(double monto, String dato) {
        String celular = dato == null ? "" : dato.trim();
        if (!celular.matches("9\\d{8}")) {
            throw new ReglaNegocioException("El celular debe tener 9 dígitos y empezar con 9");
        }
        return nuevaReferencia("YP");
    }
}
