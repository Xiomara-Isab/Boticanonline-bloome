package com.botica.util;

import java.util.List;

/** Valores fijos del negocio con nombre (evita el antipatrón "números mágicos" y los textos repetidos). */
public final class Constantes {
    public static final int STOCK_BAJO = 10;
    public static final int DIAS_POR_VENCER = 90;
    public static final int LONGITUD_MINIMA_CONTRASENA = 6;

    // Estados del pedido (patrón State)
    public static final String PENDIENTE = "PENDIENTE";
    public static final String PAGADO = "PAGADO";
    public static final String EN_DESPACHO = "EN_DESPACHO";
    public static final String ENVIADO = "ENVIADO";
    public static final String ENTREGADO = "ENTREGADO";

    // Roles (actores del sistema)
    public static final String ROL_CLIENTE = "Cliente";
    public static final String ROL_VENDEDOR = "Vendedor";
    public static final String ROL_ALMACEN = "Almacen";
    public static final String ROL_SOPORTE = "Soporte";
    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final List<String> ROLES =
            List.of(ROL_CLIENTE, ROL_VENDEDOR, ROL_ALMACEN, ROL_SOPORTE, ROL_ADMINISTRADOR);

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
