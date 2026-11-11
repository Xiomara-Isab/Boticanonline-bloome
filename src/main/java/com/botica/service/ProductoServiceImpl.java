package com.botica.service;

import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Producto;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import java.time.LocalDate;

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

    @Override
    public void cambiarActivo(int idProducto, boolean activo) {
        obtener(idProducto);
        productoDAO.actualizarActivo(idProducto, activo);
    }

    @Override
    public void aplicarDescuento(int idProducto, int porcentaje) {
        if (porcentaje < 0 || porcentaje > Constantes.DESCUENTO_MAXIMO) {
            throw new ReglaNegocioException("El descuento debe estar entre 0 y " + Constantes.DESCUENTO_MAXIMO + " %");
        }
        obtener(idProducto);
        productoDAO.actualizarDescuento(idProducto, porcentaje);
    }

    @Override
    public void registrarIngreso(int idProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad que ingresa debe ser mayor que cero");
        }
        obtener(idProducto);
        productoDAO.sumarStock(idProducto, cantidad);
    }

    @Override
    public int retirarVencidos() {
        int retirados = 0;
        LocalDate hoy = LocalDate.now();
        for (Producto producto : productoDAO.listarTodos()) {
            if (producto.getFechaVencimiento() != null && producto.getFechaVencimiento().isBefore(hoy)
                    && producto.getStock() > 0) {
                producto.setStock(0);
                productoDAO.actualizar(producto);
                retirados++;
            }
        }
        return retirados;
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
