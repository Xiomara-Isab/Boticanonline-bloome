package com.botica.dao;

import com.botica.model.ProductoVendido;
import com.botica.model.VentaDia;
import com.botica.util.ConexionBD;
import com.botica.util.Constantes;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Qué cuenta como venta: un pedido con su pago aprobado (fila de pago que no es REEMBOLSO) y que no está
 * PENDIENTE ni CANCELADO. Un reembolso pasa el pedido a CANCELADO y agrega un pago negativo: el pedido sale
 * del reporte por su estado, y el pago negativo nunca se suma. La fecha de la venta es la del pago, no la del pedido.
 */
public class ReporteDAOImpl implements ReporteDAO {
    private static final String VENTAS_FROM =
            "FROM pedido p JOIN pago pg ON pg.id_pedido = p.id AND pg.metodo <> ? ";
    private static final String VENTAS_WHERE =
            "WHERE p.estado NOT IN (?, ?) AND pg.fecha >= ? AND pg.fecha < ? ";

    @Override
    public List<VentaDia> ventasPorDia(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT CAST(pg.fecha AS DATE) AS dia, COUNT(DISTINCT p.id) AS pedidos, SUM(pg.monto) AS total "
                + VENTAS_FROM + VENTAS_WHERE + "GROUP BY dia ORDER BY dia";
        List<VentaDia> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            filtrar(ps, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new VentaDia(rs.getDate("dia").toLocalDate(), rs.getInt("pedidos"), rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular las ventas", e);
        }
        return lista;
    }

    @Override
    public List<ProductoVendido> masVendidos(LocalDate desde, LocalDate hasta, int limite) {
        String sql = "SELECT pr.id, pr.nombre, SUM(d.cantidad) AS unidades, SUM(d.subtotal) AS total "
                + VENTAS_FROM + "JOIN detalle_pedido d ON d.id_pedido = p.id JOIN producto pr ON pr.id = d.id_producto "
                + VENTAS_WHERE + "GROUP BY pr.id, pr.nombre ORDER BY unidades DESC, total DESC, pr.nombre LIMIT ?";
        List<ProductoVendido> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql)) {
            filtrar(ps, desde, hasta);
            ps.setInt(6, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ProductoVendido(rs.getInt("id"), rs.getString("nombre"), rs.getInt("unidades"),
                            rs.getDouble("total")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular los productos más vendidos", e);
        }
        return lista;
    }

    /** Parámetros 1-5 de VENTAS_FROM y VENTAS_WHERE; "hasta" se convierte en el inicio del día siguiente para incluir todo ese día. */
    private void filtrar(PreparedStatement ps, LocalDate desde, LocalDate hasta) throws SQLException {
        ps.setString(1, Constantes.PAGO_REEMBOLSO);
        ps.setString(2, Constantes.PENDIENTE);
        ps.setString(3, Constantes.CANCELADO);
        ps.setDate(4, Date.valueOf(desde));
        ps.setDate(5, Date.valueOf(hasta.plusDays(1)));
    }
}
