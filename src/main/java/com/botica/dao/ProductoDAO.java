package com.botica.dao;

import com.botica.model.Producto;
import java.sql.Connection;
import java.util.List;

public interface ProductoDAO {
    void crear(Producto p);
    Producto buscarPorId(int id);
    List<Producto> listarTodos();
    void actualizar(Producto p);
    void eliminar(int id);
    Producto buscarPorIdParaActualizar(int id, Connection conn);
    void descontarStock(int idProducto, int cantidad, Connection conn);
}
