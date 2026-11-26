package com.botica.dao;

import com.botica.model.Categoria;
import com.botica.util.ConexionBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {
    private static final String SELECT_CON_CONTEO =
            "SELECT c.*, (SELECT COUNT(*) FROM producto p WHERE p.categoria = c.nombre) AS cantidad FROM categoria c ";

    @Override
    public void crear(Categoria c) {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?,?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActiva());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear categoría", e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) {
        return buscarUna(SELECT_CON_CONTEO + "WHERE c.id = ?", id);
    }

    /** Sin distinguir mayúsculas ni espacios de los extremos: "vitaminas " y "Vitaminas" son la misma. */
    @Override
    public Categoria buscarPorNombre(String nombre) {
        return buscarUna(SELECT_CON_CONTEO + "WHERE LOWER(c.nombre) = LOWER(TRIM(?))", nombre);
    }

    @Override
    public List<Categoria> listarTodas() {
        return listar(SELECT_CON_CONTEO + "ORDER BY c.nombre");
    }

    @Override
    public List<Categoria> listarActivas() {
        return listar(SELECT_CON_CONTEO + "WHERE c.activa ORDER BY c.nombre");
    }

    /** Los productos se actualizan solos por el ON UPDATE CASCADE de la clave foránea. */
    @Override
    public void renombrar(int id, String nombre) {
        actualizar("UPDATE categoria SET nombre=? WHERE id=?", nombre, id, "Error al renombrar categoría");
    }

    @Override
    public void cambiarActiva(int id, boolean activa) {
        actualizar("UPDATE categoria SET activa=? WHERE id=?", activa, id, "Error al cambiar el estado de la categoría");
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM categoria WHERE id=?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría", e);
        }
    }

    @Override
    public int contarProductos(int id) {
        String sql = "SELECT COUNT(*) FROM producto p JOIN categoria c ON p.categoria = c.nombre WHERE c.id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar productos de la categoría", e);
        }
    }

    private void actualizar(String sql, Object valor, int id, String error) {
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setObject(1, valor);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(error, e);
        }
    }

    private Categoria buscarUna(String sql, Object parametro) {
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría", e);
        }
        return null;
    }

    private List<Categoria> listar(String sql) {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías", e);
        }
        return lista;
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setActiva(rs.getBoolean("activa"));
        c.setCantidadProductos(rs.getInt("cantidad"));
        return c;
    }
}
