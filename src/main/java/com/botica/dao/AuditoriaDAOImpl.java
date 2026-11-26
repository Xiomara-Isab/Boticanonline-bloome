package com.botica.dao;

import com.botica.model.Auditoria;
import com.botica.util.ConexionBD;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrar(Auditoria a) {
        String sql = "INSERT INTO auditoria (id_usuario, rol, accion, entidad, id_entidad, detalle, ip) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setObject(1, a.getIdUsuario(), Types.INTEGER);
            ps.setString(2, a.getRol());
            ps.setString(3, a.getAccion());
            ps.setString(4, a.getEntidad());
            ps.setObject(5, a.getIdEntidad(), Types.INTEGER);
            ps.setString(6, a.getDetalle());
            ps.setString(7, a.getIp());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar auditoría", e);
        }
    }

    @Override
    public List<Auditoria> listar(LocalDate desde, LocalDate hasta, String usuario, String accion, int limite) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.*, u.correo FROM auditoria a LEFT JOIN usuario u ON u.id = a.id_usuario WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (desde != null) {
            sql.append(" AND a.fecha >= ?");
            params.add(Timestamp.valueOf(desde.atStartOfDay()));
        }
        if (hasta != null) {
            sql.append(" AND a.fecha < ?");
            params.add(Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        }
        if (usuario != null && !usuario.isBlank()) {
            // el detalle guarda el correo de los logins fallidos, que no tienen id_usuario
            sql.append(" AND (u.correo ILIKE ? OR u.nombre ILIKE ? OR a.detalle ILIKE ?)");
            String patron = "%" + usuario.trim() + "%";
            params.add(patron);
            params.add(patron);
            params.add(patron);
        }
        if (accion != null && !accion.isBlank()) {
            sql.append(" AND a.accion = ?");
            params.add(accion.trim());
        }
        sql.append(" ORDER BY a.fecha DESC, a.id DESC LIMIT ?");
        params.add(limite);

        List<Auditoria> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Auditoria a = new Auditoria();
                    a.setId(rs.getInt("id"));
                    a.setIdUsuario((Integer) rs.getObject("id_usuario"));
                    a.setRol(rs.getString("rol"));
                    a.setAccion(rs.getString("accion"));
                    a.setEntidad(rs.getString("entidad"));
                    a.setIdEntidad((Integer) rs.getObject("id_entidad"));
                    a.setDetalle(rs.getString("detalle"));
                    a.setIp(rs.getString("ip"));
                    Timestamp ts = rs.getTimestamp("fecha");
                    if (ts != null) a.setFecha(ts.toLocalDateTime());
                    a.setCorreoUsuario(rs.getString("correo"));
                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar la auditoría", e);
        }
        return lista;
    }
}
