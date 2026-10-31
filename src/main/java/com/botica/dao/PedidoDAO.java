package com.botica.dao;

import com.botica.model.Pedido;
import java.sql.Connection;
import java.util.List;

public interface PedidoDAO {
    void crear(Pedido p);
    void crear(Pedido p, Connection conn);
    Pedido buscarPorId(int id);
    void actualizar(Pedido p);
    void actualizar(Pedido p, Connection conn);
    List<Pedido> listarTodos();
    List<Pedido> listarPorCliente(int idCliente);
}
