package com.botica.patterns.command;

/** Patrón Command (Comportamiento): una acción convertida en objeto, que sabe ejecutarse y deshacerse. */
public interface Comando {
    void ejecutar();

    void deshacer();

    /** Texto para mostrar al usuario qué hace este comando. */
    String descripcion();
}
