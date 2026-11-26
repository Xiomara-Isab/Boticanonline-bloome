package com.botica.service;

import com.botica.dao.ProductoDAO;
import com.botica.model.Carrito;
import com.botica.model.Producto;
import com.botica.patterns.facade.ItemCarrito;
import com.botica.util.ReglaNegocioException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprobación sin base de datos del carrito (reglas de stock, productos desactivados y cantidades).
 * Ejecutar con: java -ea -cp "target/classes:target/test-classes" com.botica.service.CarritoServiceCheck
 */
public class CarritoServiceCheck {
    private static final Map<Integer, Producto> BD = new HashMap<>();

    private static final ProductoDAO DAO = new ProductoDAO() {
        @Override public void crear(Producto p) { }
        @Override public Producto buscarPorId(int id) { return BD.get(id); }
        @Override public List<Producto> listarTodos() { return new ArrayList<>(BD.values()); }
        @Override public void actualizar(Producto p) { }
        @Override public void eliminar(int id) { }
        @Override public Producto buscarPorIdParaActualizar(int id, Connection conn) { return BD.get(id); }
        @Override public void descontarStock(int idProducto, int cantidad, Connection conn) { }
        @Override public void sumarStock(int idProducto, int cantidad) { }
        @Override public void sumarStock(int idProducto, int cantidad, Connection conn) { }
        @Override public void actualizarActivo(int idProducto, boolean activo) { }
        @Override public void actualizarDescuento(int idProducto, int descuento) { }
    };

    private static Producto producto(int id, String nombre, int stock, boolean activo) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setStock(stock);
        p.setActivo(activo);
        BD.put(id, p);
        return p;
    }

    public static void main(String[] args) {
        producto(1, "Paracetamol", 10, true);
        producto(2, "Alcohol", 3, true);
        producto(3, "Descontinuado", 50, false);
        producto(4, "Agotado", 0, true);
        CarritoService servicio = new CarritoService(DAO);
        Carrito carrito = new Carrito();

        // agregar: suma cantidades del mismo producto y devuelve el producto
        assert servicio.agregar(carrito, 1, 2).getNombre().equals("Paracetamol");
        servicio.agregar(carrito, 1, 3);
        servicio.agregar(carrito, 2, 1);
        assert carrito.cantidadDe(1) == 5 && carrito.cantidadDe(2) == 1;
        assert carrito.getCantidadTotal() == 6 && !carrito.isVacio();

        // reglas: stock, desactivados, agotados, cantidades inválidas
        rechaza(() -> servicio.agregar(carrito, 1, 6));   // 5 + 6 > 10
        rechaza(() -> servicio.agregar(carrito, 3, 1));   // producto desactivado
        rechaza(() -> servicio.agregar(carrito, 4, 1));   // agotado
        rechaza(() -> servicio.agregar(carrito, 99, 1));  // no existe
        rechaza(() -> servicio.agregar(carrito, 1, 0));
        assert carrito.cantidadDe(1) == 5 : "un intento rechazado no debe cambiar el carrito";

        // cambiar cantidad: respeta el stock; 0 quita la línea
        servicio.cambiarCantidad(carrito, 1, 8);
        assert carrito.cantidadDe(1) == 8;
        rechaza(() -> servicio.cambiarCantidad(carrito, 2, 4));  // solo hay 3
        servicio.cambiarCantidad(carrito, 2, 0);
        assert carrito.cantidadDe(2) == 0;

        // items para el Facade: un solo pedido con varios productos
        servicio.agregar(carrito, 2, 2);
        List<ItemCarrito> items = servicio.items(carrito);
        assert items.size() == 2;
        assert items.get(0).getIdProducto() == 1 && items.get(0).getCantidad() == 8;
        assert items.get(1).getIdProducto() == 2 && items.get(1).getCantidad() == 2;

        // quitar y vaciar
        servicio.quitar(carrito, 1);
        assert carrito.cantidadDe(1) == 0 && !carrito.isVacio();
        carrito.vaciar();
        assert carrito.isVacio() && carrito.getCantidadTotal() == 0;
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
