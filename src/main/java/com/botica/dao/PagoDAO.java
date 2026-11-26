package com.botica.dao;

import com.botica.model.Pago;
import java.sql.Connection;
import java.util.List;

public interface PagoDAO {
    void crear(Pago pago, Connection conn);
    List<Pago> listarPorPedido(int idPedido);
}
