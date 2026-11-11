package com.botica.patterns.pago;

import com.botica.model.Producto;
import com.botica.patterns.EstadoPedidoFactory;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;

/**
 * Comprobación sin base de datos del pago simulado (Strategy + Factory), del precio con descuento y del estado CANCELADO.
 * Ejecutar con:
 * mvn -q test-compile &&
 * java -ea -cp "target/classes:target/test-classes" com.botica.patterns.pago.MetodoPagoCheck
 */
public class MetodoPagoCheck {
    public static void main(String[] args) {
        // Factory: devuelve la estrategia correcta y rechaza códigos desconocidos
        assert MetodoPagoFactory.desde(Constantes.PAGO_TARJETA) instanceof PagoTarjeta;
        assert MetodoPagoFactory.desde(Constantes.PAGO_YAPE) instanceof PagoYape;
        assert MetodoPagoFactory.desde(Constantes.PAGO_MERCADO_PAGO) instanceof PagoMercadoPago;
        assert MetodoPagoFactory.disponibles().size() == 3;
        rechaza(() -> MetodoPagoFactory.desde("EFECTIVO"));

        // Strategy: cada método valida su propio dato y devuelve un código de operación
        assert MetodoPagoFactory.desde(Constantes.PAGO_TARJETA).procesar(10, "4111 1111 1111 1111").startsWith("TJ-");
        assert MetodoPagoFactory.desde(Constantes.PAGO_YAPE).procesar(10, "987654321").startsWith("YP-");
        assert MetodoPagoFactory.desde(Constantes.PAGO_MERCADO_PAGO).procesar(10, "ana@correo.com").startsWith("MP-");
        rechaza(() -> MetodoPagoFactory.desde(Constantes.PAGO_TARJETA).procesar(10, "1234"));
        rechaza(() -> MetodoPagoFactory.desde(Constantes.PAGO_TARJETA).procesar(10, "4111 1111 1111 0000"));
        rechaza(() -> MetodoPagoFactory.desde(Constantes.PAGO_YAPE).procesar(10, "12345"));
        rechaza(() -> MetodoPagoFactory.desde(Constantes.PAGO_MERCADO_PAGO).procesar(10, "sin-arroba"));

        // precio con descuento
        Producto p = new Producto();
        p.setPrecio(8.50);
        assert p.getPrecioFinal() == 8.50;
        p.setDescuento(20);
        assert p.getPrecioFinal() == 6.80 : p.getPrecioFinal();

        // State: CANCELADO es un estado final que no avanza
        assert EstadoPedidoFactory.desde(Constantes.CANCELADO).esFinal();
        System.out.println("OK");
    }

    private static void rechaza(Runnable accion) {
        try {
            accion.run();
            assert false : "debía rechazarse";
        } catch (ReglaNegocioException esperado) {
            // correcto
        }
    }
}
