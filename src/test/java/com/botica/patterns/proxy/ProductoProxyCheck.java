package com.botica.patterns.proxy;

import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.service.ProductoService;
import com.botica.util.Constantes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Comprobación sin base de datos del guardia del catálogo. Ejecutar con:
 * mvn -q test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt &&
 * java -ea -cp "target/classes:target/test-classes:$(cat target/cp.txt)" com.botica.patterns.proxy.ProductoProxyCheck
 */
public class ProductoProxyCheck {
    /** Servicio real falso: anota qué acciones le llegaron. */
    private static final List<String> LLEGARON = new ArrayList<>();
    private static final ProductoService REAL = new ProductoService() {
        @Override public void crear(Producto p) { LLEGARON.add("crear"); }
        @Override public void actualizarPrecioYStock(int id, double precio, int stock) { LLEGARON.add("editar"); }
        @Override public void ajustarStock(int id, int stock) { LLEGARON.add("stock"); }
        @Override public void eliminar(int id) { LLEGARON.add("eliminar"); }
    };

    public static void main(String[] args) {
        List<String> registro = new ArrayList<>();
        Logger logger = Logger.getLogger(ProductoServiceProxy.class.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { registro.add(r.getMessage()); }
            @Override public void flush() { }
            @Override public void close() { }
        });

        ProductoService vendedor = new ProductoServiceProxy(REAL, usuario("vendedor@botica.com", Constantes.ROL_VENDEDOR));
        ProductoService almacen = new ProductoServiceProxy(REAL, usuario("almacen@botica.com", Constantes.ROL_ALMACEN));
        ProductoService cliente = new ProductoServiceProxy(REAL, usuario("cliente@botica.com", Constantes.ROL_CLIENTE));
        ProductoService admin = new ProductoServiceProxy(REAL, usuario("admin@botica.com", Constantes.ROL_ADMINISTRADOR));

        // con permiso, la acción llega al servicio real y queda registrada como PERMITIDO
        vendedor.actualizarPrecioYStock(1, 9.9, 5);
        almacen.ajustarStock(1, 20);
        admin.eliminar(1);
        assert LLEGARON.equals(List.of("editar", "stock", "eliminar")) : LLEGARON;
        assert registro.get(0).equals("vendedor@botica.com (Vendedor) intentó editar el producto #1: PERMITIDO") : registro;

        // sin permiso, el guardia corta antes del servicio real y registra quién lo intentó
        LLEGARON.clear();
        denegado(() -> almacen.actualizarPrecioYStock(1, 9.9, 5));
        denegado(() -> vendedor.ajustarStock(1, 20));
        denegado(() -> cliente.eliminar(1));
        denegado(() -> cliente.crear(new Producto()));
        assert LLEGARON.isEmpty() : LLEGARON;
        assert registro.contains("almacen@botica.com (Almacen) intentó editar el producto #1: DENEGADO") : registro;
        assert registro.contains("cliente@botica.com (Cliente) intentó eliminar el producto #1: DENEGADO") : registro;
        System.out.println("OK");
    }

    private static Usuario usuario(String correo, String rol) {
        return new Usuario("demo", correo, null, rol);
    }

    private static void denegado(Runnable accion) {
        try {
            accion.run();
            assert false : "el guardia debía bloquearlo";
        } catch (SecurityException esperado) {
            assert esperado.getMessage().startsWith("No tienes permiso");
        }
    }
}
