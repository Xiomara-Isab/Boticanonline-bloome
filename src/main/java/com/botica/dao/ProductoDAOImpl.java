package com.botica.dao;

import com.botica.model.Producto;
import com.botica.util.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    @Override
    public void crear(Producto p) {
        String sql = "INSERT INTO producto (nombre, descripcion, precio, stock, categoria, fecha_vencimiento, id_vendedor) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategoria());
            ps.setDate(6, p.getFechaVencimiento() != null ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setInt(7, p.getIdVendedor());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear producto", e);
        }
    }

    @Override
    public Producto buscarPorId(int id) {
        String sql = "SELECT * FROM producto WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto", e);
        }
        return null;
    }

    @Override
    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre=?, descripcion=?, precio=?, stock=?, categoria=?, fecha_vencimiento=? WHERE id=?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategoria());
            ps.setDate(6, p.getFechaVencimiento() != null ? Date.valueOf(p.getFechaVencimiento()) : null);
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto", e);
        }
    }

    @Override
    public Producto buscarPorIdParaActualizar(int id, Connection conn) {
        String sql = "SELECT * FROM producto WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al bloquear producto", e);
        }
        return null;
    }

    @Override
    public void descontarStock(int idProducto, int cantidad, Connection conn) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al descontar stock", e);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getDouble("precio"));
        p.setStock(rs.getInt("stock"));
        p.setCategoria(rs.getString("categoria"));
        Date fecha = rs.getDate("fecha_vencimiento");
        if (fecha != null) p.setFechaVencimiento(fecha.toLocalDate());
        p.setIdVendedor(rs.getInt("id_vendedor"));
        return p;
    }
}
