package com.botica.service;

import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Producto;
import com.botica.util.ReglaNegocioException;

/** Reglas de negocio del catálogo: validación de datos y control de precios y stock. */
public class ProductoService {
    private final ProductoDAO productoDAO;

    public ProductoService() {
        this(DAOFactory.getProductoDAO());
    }

    public ProductoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    public void crear(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio");
        }
        validarPrecio(producto.getPrecio());
        validarStock(producto.getStock());
        productoDAO.crear(producto);
    }

    public void actualizarPrecioYStock(int idProducto, double precio, int stock) {
        validarPrecio(precio);
        validarStock(stock);
        Producto producto = obtener(idProducto);
        producto.setPrecio(precio);
        producto.setStock(stock);
        productoDAO.actualizar(producto);
    }

    public void ajustarStock(int idProducto, int stock) {
        validarStock(stock);
        Producto producto = obtener(idProducto);
        producto.setStock(stock);
        productoDAO.actualizar(producto);
    }

    public void eliminar(int idProducto) {
        try {
            productoDAO.eliminar(idProducto);
        } catch (RuntimeException e) {
            throw new ReglaNegocioException("No se puede eliminar: el producto ya tiene pedidos asociados");
        }
    }

    private Producto obtener(int idProducto) {
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            throw new ReglaNegocioException("Producto no encontrado");
        }
        return producto;
    }

    private void validarPrecio(double precio) {
        if (precio <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero");
        }
    }

    private void validarStock(int stock) {
        if (stock < 0) {
            throw new ReglaNegocioException("El stock no puede ser negativo");
        }
    }
}
