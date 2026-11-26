package com.botica.service;

import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Carrito;
import com.botica.model.Producto;
import com.botica.patterns.facade.ItemCarrito;
import com.botica.util.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reglas del carrito: un producto debe estar activo y la cantidad no puede superar el stock. */
public class CarritoService {
    private final ProductoDAO productoDAO;

    public CarritoService() {
        this(DAOFactory.getProductoDAO());
    }

    public CarritoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    /** Agrega unidades al carrito. Devuelve el producto para poder mostrar su nombre en el mensaje. */
    public Producto agregar(Carrito carrito, int idProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        Producto producto = disponible(idProducto);
        int yaEnCarrito = carrito.cantidadDe(idProducto);
        if (yaEnCarrito + cantidad > producto.getStock()) {
            throw new ReglaNegocioException("Solo hay " + producto.getStock() + " unidades de " + producto.getNombre()
                    + (yaEnCarrito > 0 ? " (ya tienes " + yaEnCarrito + " en el carrito)" : ""));
        }
        carrito.agregar(idProducto, cantidad);
        return producto;
    }

    /** Cambia la cantidad de una línea; con 0 la quita. */
    public void cambiarCantidad(Carrito carrito, int idProducto, int cantidad) {
        if (cantidad <= 0) {
            carrito.quitar(idProducto);
            return;
        }
        Producto producto = disponible(idProducto);
        if (cantidad > producto.getStock()) {
            throw new ReglaNegocioException("Solo hay " + producto.getStock() + " unidades de " + producto.getNombre());
        }
        carrito.fijarCantidad(idProducto, cantidad);
    }

    public void quitar(Carrito carrito, int idProducto) {
        carrito.quitar(idProducto);
    }

    /** Convierte el carrito en la lista que espera PedidoFacade.realizarPedido (un solo pedido, varios productos). */
    public List<ItemCarrito> items(Carrito carrito) {
        List<ItemCarrito> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> linea : carrito.getLineas().entrySet()) {
            items.add(new ItemCarrito(linea.getKey(), linea.getValue()));
        }
        return items;
    }

    private Producto disponible(int idProducto) {
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null || !producto.isActivo()) {
            throw new ReglaNegocioException("El producto ya no está disponible");
        }
        if (producto.getStock() <= 0) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " está agotado");
        }
        return producto;
    }
}
