package com.botica.dao;

import com.botica.model.Reclamo;
import com.botica.util.ConexionBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReclamoDAOImpl implements ReclamoDAO {

    @Override
    public void crear(Reclamo r) {
        String sql = "INSERT INTO reclamo (id_pedido, descripcion, estado) VALUES (?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, r.getIdPedido());
            ps.setString(2, r.getDescripcion());
            ps.setString(3, r.getEstado());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear reclamo", e);
        }
    }

    @Override
    public Reclamo buscarPorId(int id) {
        List<Reclamo> lista = listar("SELECT * FROM reclamo WHERE id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Reclamo> listarTodos() {
        return listar("SELECT * FROM reclamo ORDER BY fecha_creacion DESC, id DESC", null);
    }

    @Override
    public List<Reclamo> listarPorCliente(int idCliente) {
        return listar("SELECT r.* FROM reclamo r JOIN pedido p ON p.id = r.id_pedido "
                + "WHERE p.id_cliente = ? ORDER BY r.fecha_creacion DESC, r.id DESC", idCliente);
    }

    @Override
    public void actualizarEstado(int id, String estado) {
        String sql = "UPDATE reclamo SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar reclamo", e);
        }
    }

    private List<Reclamo> listar(String sql, Integer param) {
        List<Reclamo> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reclamo r = new Reclamo();
                    r.setId(rs.getInt("id"));
                    r.setIdPedido(rs.getInt("id_pedido"));
                    r.setDescripcion(rs.getString("descripcion"));
                    r.setEstado(rs.getString("estado"));
                    Timestamp ts = rs.getTimestamp("fecha_creacion");
                    if (ts != null) r.setFechaCreacion(ts.toLocalDateTime());
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar reclamos", e);
        }
        return lista;
    }
}
