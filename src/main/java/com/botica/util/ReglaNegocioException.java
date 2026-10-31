package com.botica.util;

/** Error de regla de negocio (datos inválidos, stock insuficiente, duplicados...). Su mensaje es apto para mostrarse al usuario. */
public class ReglaNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
