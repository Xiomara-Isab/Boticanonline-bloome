package com.botica.service;

import com.botica.model.Producto;

/**
 * Operaciones del catálogo. La implementan el servicio real (ProductoServiceImpl) y su guardia
 * (ProductoServiceProxy), así quien lo usa no distingue uno del otro.
 */
public interface ProductoService {
    void crear(Producto producto);

    void actualizarPrecioYStock(int idProducto, double precio, int stock);

    void ajustarStock(int idProducto, int stock);

    void eliminar(int idProducto);
}
