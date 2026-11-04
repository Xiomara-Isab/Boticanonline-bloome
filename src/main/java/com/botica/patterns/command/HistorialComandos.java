package com.botica.patterns.command;

import com.botica.util.ReglaNegocioException;
import java.util.ArrayDeque;
import java.util.Deque;

/** Invocador del patrón Command: ejecuta los comandos y los apila para deshacer el último. Hay uno por sesión. */
public class HistorialComandos {
    private final Deque<Comando> pila = new ArrayDeque<>();

    public synchronized void ejecutar(Comando comando) {
        comando.ejecutar();
        pila.push(comando);
    }

    /** Deshace el último comando y lo devuelve. */
    public synchronized Comando deshacer() {
        Comando comando = pila.peek();
        if (comando == null) {
            throw new ReglaNegocioException("No hay cambios para deshacer");
        }
        try {
            comando.deshacer();
        } catch (ReglaNegocioException e) {
            pila.pop(); // ya nunca se podrá deshacer; un error de BD, en cambio, lo deja en la pila para reintentar
            throw e;
        }
        pila.pop();
        return comando;
    }

    /** Descripción del comando que se desharía, o null si no hay ninguno. */
    public synchronized String ultimaDescripcion() {
        Comando comando = pila.peek();
        return comando == null ? null : comando.descripcion();
    }
}
