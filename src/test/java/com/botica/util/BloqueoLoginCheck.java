package com.botica.util;

/**
 * Comprobación del bloqueo temporal de login (sin base de datos, con reloj simulado).
 * Ejecutar con: java -ea -cp "target/classes:target/test-classes" com.botica.util.BloqueoLoginCheck
 */
public class BloqueoLoginCheck {
    private static long ahora = 0;

    public static void main(String[] args) {
        BloqueoLogin b = new BloqueoLogin(5, 15 * 60_000L, () -> ahora);
        for (int quedan = 4; quedan >= 1; quedan--) {
            assert b.registrarFallo("Ana@Botica.com ") == quedan : "debe descontar intentos";
            assert b.minutosRestantes("ana@botica.com") == 0 : "aún no bloqueado";
        }
        assert b.registrarFallo("ana@botica.com") == 0 : "el 5.º fallo bloquea";
        assert b.minutosRestantes("ana@botica.com") == 15 : "15 minutos de bloqueo";
        assert b.minutosRestantes("otro@botica.com") == 0 : "otros correos no se bloquean";

        ahora += 14 * 60_000L + 1;
        assert b.minutosRestantes("ana@botica.com") == 1 : "redondea hacia arriba";
        ahora += 60_000L;
        assert b.minutosRestantes("ana@botica.com") == 0 : "el bloqueo expira";
        assert b.registrarFallo("ana@botica.com") == 4 : "tras expirar vuelve a tener 5 intentos";

        b.registrarExito("ana@botica.com");
        assert b.registrarFallo("ana@botica.com") == 4 : "un login correcto reinicia el contador";
        System.out.println("BloqueoLoginCheck OK");
    }
}
