package com.botica.patterns.pago;

import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/** Estrategia: tarjeta de crédito o débito (simulada). */
public class PagoTarjeta implements MetodoPago {
    @Override
    public String codigo() {
        return Constantes.PAGO_TARJETA;
    }

    @Override
    public String etiqueta() {
        return "Tarjeta de crédito / débito";
    }

    @Override
    public String ayuda() {
        return "Número de tarjeta (16 dígitos).";
    }

    @Override
    public String procesar(double monto, String dato) {
        String numero = dato == null ? "" : dato.replaceAll("[\\s-]", "");
        if (!numero.matches("\\d{16}")) {
            throw new ReglaNegocioException("La tarjeta debe tener 16 dígitos");
        }
        if (numero.endsWith("0000")) {
            throw new ReglaNegocioException("Pago rechazado por el banco");
        }
        return nuevaReferencia("TJ");
    }
}
