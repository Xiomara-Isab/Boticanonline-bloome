package com.botica.dao;

import com.botica.model.Usuario;
import com.botica.util.ConexionBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public void crear(Usuario u) {
        String sql = "INSERT INTO usuario (nombre, correo, contrasena, rol) VALUES (?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getCorreo());
            ps.setString(3, u.getContrasena());
            ps.setString(4, u.getRol());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear usuario", e);
        }
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario", e);
        }
        return null;
    }

    @Override
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario", e);
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios", e);
        }
        return lista;
    }

    @Override
    public void actualizarRol(int id, String rol) {
        String sql = "UPDATE usuario SET rol = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, rol);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar rol", e);
        }
    }

    @Override
    public void actualizarActivo(int id, boolean activo) {
        String sql = "UPDATE usuario SET activo = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar el estado del usuario", e);
        }
    }

    @Override
    public void actualizarContrasena(int id, String contrasenaHasheada) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id = ?";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setString(1, contrasenaHasheada);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la contraseña", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
