package com.botica.dao;

import com.botica.model.Pedido;
import com.botica.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAOImpl implements PedidoDAO {

    @Override
    public void crear(Pedido p) {
        crear(p, ConexionBD.getInstancia().getConexion());
    }

    @Override
    public void crear(Pedido p, Connection conn) {
        String sql = "INSERT INTO pedido (id_cliente, estado, total) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdCliente());
            ps.setString(2, p.getEstado());
            ps.setDouble(3, p.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear pedido", e);
        }
    }

    @Override
    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM pedido WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido", e);
        }
        return null;
    }

    @Override
    public void actualizar(Pedido p) {
        actualizar(p, ConexionBD.getInstancia().getConexion());
    }

    @Override
    public void actualizar(Pedido p, Connection conn) {
        String sql = "UPDATE pedido SET estado=?, total=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getEstado());
            ps.setDouble(2, p.getTotal());
            ps.setInt(3, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar pedido", e);
        }
    }

    @Override
    public List<Pedido> listarTodos() {
        return listar("SELECT * FROM pedido ORDER BY fecha_pedido DESC, id DESC", null);
    }

    @Override
    public List<Pedido> listarPorCliente(int idCliente) {
        return listar("SELECT * FROM pedido WHERE id_cliente = ? ORDER BY fecha_pedido DESC, id DESC", idCliente);
    }

    private List<Pedido> listar(String sql, Integer param) {
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos", e);
        }
        return lista;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setEstado(rs.getString("estado"));
        p.setTotal(rs.getDouble("total"));
        Timestamp ts = rs.getTimestamp("fecha_pedido");
        if (ts != null) p.setFechaPedido(ts.toLocalDateTime());
        return p;
    }
}
