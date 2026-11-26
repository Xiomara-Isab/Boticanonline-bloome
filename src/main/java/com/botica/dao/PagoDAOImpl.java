package com.botica.dao;

import com.botica.model.Pago;
import com.botica.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PagoDAOImpl implements PagoDAO {

    @Override
    public void crear(Pago pago, Connection conn) {
        String sql = "INSERT INTO pago (id_pedido, metodo, monto, referencia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pago.getIdPedido());
            ps.setString(2, pago.getMetodo());
            ps.setDouble(3, pago.getMonto());
            ps.setString(4, pago.getReferencia());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el pago", e);
        }
    }

    @Override
    public List<Pago> listarPorPedido(int idPedido) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pago WHERE id_pedido = ? ORDER BY id";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getInt("id"));
                    p.setIdPedido(rs.getInt("id_pedido"));
                    p.setMetodo(rs.getString("metodo"));
                    p.setMonto(rs.getDouble("monto"));
                    p.setReferencia(rs.getString("referencia"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) p.setFecha(ts.toLocalDateTime());
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pagos", e);
        }
        return lista;
    }
}
