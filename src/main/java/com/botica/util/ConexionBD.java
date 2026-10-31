package com.botica.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Patrón Singleton (Creacional): una única instancia que administra la única conexión JDBC del sistema.
 * La configuración se lee de variables de entorno (BOTICA_DB_URL, BOTICA_DB_USER, BOTICA_DB_PASSWORD)
 * y, si no existen, usa valores de desarrollo local.
 */
public class ConexionBD {
    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String password;
    private Connection conexion;

    private ConexionBD() {
        this.url = leer("BOTICA_DB_URL", "jdbc:postgresql://localhost:5432/botica_db");
        this.usuario = leer("BOTICA_DB_USER", "postgres");
        this.password = leer("BOTICA_DB_PASSWORD", "admin");
        this.conexion = abrir();
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = abrir();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar la conexión a la base de datos", e);
        }
        return conexion;
    }

    /**
     * Ejecuta un bloque de trabajo como una transacción atómica (commit si termina bien, rollback si falla).
     * Es synchronized para que dos pedidos no mezclen sus operaciones sobre la misma conexión compartida.
     */
    public synchronized <T> T ejecutarTransaccion(Function<Connection, T> trabajo) {
        Connection conn = getConexion();
        try {
            conn.setAutoCommit(false);
            T resultado = trabajo.apply(conn);
            conn.commit();
            return resultado;
        } catch (RuntimeException e) {
            revertir(conn);
            throw e;
        } catch (SQLException e) {
            revertir(conn);
            throw new RuntimeException("Error en la transacción", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Error al restaurar autocommit", e);
            }
        }
    }

    private void revertir(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Error al revertir la transacción", e);
        }
    }

    private Connection abrir() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, usuario, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontró el driver de PostgreSQL", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar a la base de datos", e);
        }
    }

    private static String leer(String clave, String defecto) {
        String valor = System.getenv(clave);
        return (valor == null || valor.isBlank()) ? defecto : valor;
    }
}
