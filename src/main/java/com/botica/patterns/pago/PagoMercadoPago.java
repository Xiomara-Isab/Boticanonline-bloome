package com.botica.patterns.pago;

import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/**
 * Estrategia: Mercado Pago (SIMULADA). Aquí solo se valida el correo y se genera un código.
 * Para conectar la pasarela real bastaría reemplazar el cuerpo de procesar() por la llamada a su API,
 * sin tocar PagoService ni los controladores.
 */
public class PagoMercadoPago implements MetodoPago {
    @Override
    public String codigo() {
        return Constantes.PAGO_MERCADO_PAGO;
    }

    @Override
    public String etiqueta() {
        return "Mercado Pago";
    }

    @Override
    public String ayuda() {
        return "Correo de tu cuenta de Mercado Pago.";
    }

    @Override
    public String procesar(double monto, String dato) {
        String correo = dato == null ? "" : dato.trim();
        if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ReglaNegocioException("Ingresa un correo válido de Mercado Pago");
        }
        return nuevaReferencia("MP");
    }
}
