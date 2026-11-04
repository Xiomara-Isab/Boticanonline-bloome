package com.botica.service;

import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Producto;
import com.botica.util.ReglaNegocioException;

/** Servicio real del catálogo: validación de datos y control de precios y stock. */
public class ProductoServiceImpl implements ProductoService {
    private final ProductoDAO productoDAO;

    public ProductoServiceImpl() {
        this(DAOFactory.getProductoDAO());
    }

    public ProductoServiceImpl(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    @Override
    public void crear(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio");
        }
        validarPrecio(producto.getPrecio());
        validarStock(producto.getStock());
        productoDAO.crear(producto);
    }

    @Override
    public void actualizarPrecioYStock(int idProducto, double precio, int stock) {
        validarPrecio(precio);
        validarStock(stock);
        Producto producto = obtener(idProducto);
        producto.setPrecio(precio);
        producto.setStock(stock);
        productoDAO.actualizar(producto);
    }

    @Override
    public void ajustarStock(int idProducto, int stock) {
        validarStock(stock);
        Producto producto = obtener(idProducto);
        producto.setStock(stock);
        productoDAO.actualizar(producto);
    }

    @Override
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
