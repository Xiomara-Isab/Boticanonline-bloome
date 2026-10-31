package com.botica.dao;

import com.botica.model.DetallePedido;
import java.sql.Connection;
import java.util.List;

public interface DetallePedidoDAO {
    void crear(DetallePedido d, Connection conn);
    List<DetallePedido> listarPorPedido(int idPedido);
}
