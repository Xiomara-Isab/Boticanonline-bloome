package com.botica.patterns.pago;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Patrón Strategy (Comportamiento): cada método de pago es una estrategia intercambiable.
 * PagoService solo conoce esta interfaz, así que agregar un método nuevo (o cambiar la simulación
 * por una pasarela real) no obliga a modificar el servicio (principio Abierto/Cerrado de SOLID).
 */
public interface MetodoPago {
    /** Código que se guarda en la base de datos (ver Constantes.PAGO_*). */
    String codigo();

    /** Nombre que ve el cliente. */
    String etiqueta();

    /** Qué dato pide este método (texto de ayuda del formulario). */
    String ayuda();

    /** Procesa el cobro (simulado). Devuelve el código de operación o lanza ReglaNegocioException si se rechaza. */
    String procesar(double monto, String dato);

    default String nuevaReferencia(String prefijo) {
        return prefijo + "-" + ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
    }
}
