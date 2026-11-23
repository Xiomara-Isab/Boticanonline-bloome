package com.botica.util;

import java.util.List;

/** Valores fijos del negocio con nombre (evita el antipatrón "números mágicos" y los textos repetidos). */
public final class Constantes {
    public static final int STOCK_BAJO = 10;
    public static final int DIAS_POR_VENCER = 90;
    public static final int LONGITUD_MINIMA_CONTRASENA = 6;
    public static final int DESCUENTO_MAXIMO = 90;

    // Métodos de pago (patrón Strategy)
    public static final String PAGO_TARJETA = "TARJETA";
    public static final String PAGO_YAPE = "YAPE";
    public static final String PAGO_MERCADO_PAGO = "MERCADO_PAGO";
    public static final String PAGO_REEMBOLSO = "REEMBOLSO";

    // Estados del pedido (patrón State)
    public static final String PENDIENTE = "PENDIENTE";
    public static final String PAGADO = "PAGADO";
    public static final String EN_DESPACHO = "EN_DESPACHO";
    public static final String ENVIADO = "ENVIADO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    // Roles (actores del sistema)
    public static final String ROL_CLIENTE = "Cliente";
    public static final String ROL_VENDEDOR = "Vendedor";
    public static final String ROL_ALMACEN = "Almacen";
    public static final String ROL_SOPORTE = "Soporte";
    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_AUDITOR = "Auditor";
    public static final List<String> ROLES =
            List.of(ROL_CLIENTE, ROL_VENDEDOR, ROL_ALMACEN, ROL_SOPORTE, ROL_ADMINISTRADOR, ROL_AUDITOR);

    // Acciones de auditoría de la sesión (las de pedidos las pone NotificadorAuditoria)
    public static final String AUD_LOGIN = "LOGIN";
    public static final String AUD_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    public static final String AUD_LOGIN_BLOQUEADO = "LOGIN_BLOQUEADO";
    public static final String AUD_LOGOUT = "LOGOUT";
    public static final String AUD_ACCESO_DENEGADO = "ACCESO_DENEGADO";
    public static final int LOGIN_MAX_INTENTOS = 5;
    public static final int LOGIN_MINUTOS_BLOQUEO = 15;

    // Estados del reclamo
    public static final String RECLAMO_ABIERTO = "ABIERTO";
    public static final String RECLAMO_EN_PROCESO = "EN_PROCESO";
    public static final String RECLAMO_ESCALADO = "ESCALADO";
    public static final String RECLAMO_RESUELTO = "RESUELTO";
    public static final List<String> ESTADOS_RECLAMO =
            List.of(RECLAMO_ABIERTO, RECLAMO_EN_PROCESO, RECLAMO_ESCALADO, RECLAMO_RESUELTO);

    private Constantes() {
    }
}
