package com.botica.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Bloqueo temporal por correo tras demasiados logins fallidos seguidos.
 * shortcut: vive en memoria, se reinicia al reiniciar Tomcat y no se comparte entre servidores; pasar a BD si hay más de uno.
 */
public class BloqueoLogin {
    private record Estado(int fallos, long bloqueadoHasta) {
    }

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final long duracionMs;
    private final LongSupplier reloj;

    public BloqueoLogin() {
        this(Constantes.LOGIN_MAX_INTENTOS, Constantes.LOGIN_MINUTOS_BLOQUEO * 60_000L, System::currentTimeMillis);
    }

    public BloqueoLogin(int maxIntentos, long duracionMs, LongSupplier reloj) {
        this.maxIntentos = maxIntentos;
        this.duracionMs = duracionMs;
        this.reloj = reloj;
    }

    /** Minutos que faltan para poder intentar de nuevo (0 = no está bloqueado). */
    public long minutosRestantes(String correo) {
        Estado e = estados.get(clave(correo));
        long faltan = e == null ? 0 : e.bloqueadoHasta() - reloj.getAsLong();
        return faltan <= 0 ? 0 : (faltan + 59_999) / 60_000;
    }

    /** Cuenta un fallo y devuelve los intentos que quedan; 0 significa que acaba de bloquearse. */
    public int registrarFallo(String correo) {
        Estado e = estados.compute(clave(correo), (k, previo) -> {
            int fallos = (previo == null ? 0 : previo.fallos()) + 1;
            return fallos >= maxIntentos ? new Estado(0, reloj.getAsLong() + duracionMs) : new Estado(fallos, 0);
        });
        return e.bloqueadoHasta() > 0 ? 0 : maxIntentos - e.fallos();
    }

    public void registrarExito(String correo) {
        estados.remove(clave(correo));
    }

    private static String clave(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
