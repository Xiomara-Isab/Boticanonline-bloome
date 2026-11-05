package com.botica.patterns.memento;

import com.botica.dao.PedidoDAO;
import com.botica.model.Pedido;
import com.botica.patterns.PedidoSujeto;
import com.botica.service.PedidoService;
import com.botica.util.ReglaNegocioException;
import java.sql.Connection;
import java.util.List;

/**
 * Comprobación sin base de datos de revertir pedidos. Ejecutar con:
 * mvn -q test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt &&
 * java -ea -cp "target/classes:target/test-classes:$(cat target/cp.txt)" com.botica.patterns.memento.PedidoMementoCheck
 */
public class PedidoMementoCheck {
    private static final Pedido BD = new Pedido(1, "PAGADO", 45.0);
    private static boolean falloBD;

    private static final PedidoDAO DAO = new PedidoDAO() {
        @Override public void crear(Pedido p) { }
        @Override public void crear(Pedido p, Connection c) { }
        @Override public Pedido buscarPorId(int id) {
            if (id != 1) return null;
            Pedido copia = new Pedido(BD.getIdCliente(), BD.getEstado(), BD.getTotal());
            copia.setId(1);
            return copia;
        }
        @Override public void actualizar(Pedido p) {
            if (falloBD) throw new RuntimeException("BD caída");
            BD.setEstado(p.getEstado());
            BD.setTotal(p.getTotal());
        }
        @Override public void actualizar(Pedido p, Connection c) { }
        @Override public List<Pedido> listarTodos() { return List.of(); }
        @Override public List<Pedido> listarPorCliente(int idCliente) { return List.of(); }
    };

    public static void main(String[] args) {
        HistorialPedidos historial = new HistorialPedidos();
        PedidoService servicio = new PedidoService(DAO, new PedidoSujeto(), historial);

        // sin avances no hay foto
        fallaConRegla(servicio, 1);
        assert "PAGADO".equals(BD.getEstado());

        // revierte en orden inverso, de uno en uno
        servicio.avanzarEstado(1);
        servicio.avanzarEstado(1);
        assert "ENVIADO".equals(BD.getEstado());
        assert "EN_DESPACHO".equals(servicio.revertirEstado(1).getEstado());
        assert "EN_DESPACHO".equals(BD.getEstado());
        servicio.revertirEstado(1);
        assert "PAGADO".equals(BD.getEstado());
        assert BD.getTotal() == 45.0;
        assert historial.ultima(1) == null;
        fallaConRegla(servicio, 1);

        // un pedido entregado también se puede revertir
        servicio.avanzarEstado(1);
        servicio.avanzarEstado(1);
        servicio.avanzarEstado(1);
        assert "ENTREGADO".equals(BD.getEstado());
        servicio.revertirEstado(1);
        assert "ENVIADO".equals(BD.getEstado());

        // un error de BD no consume la foto, y un avance fallido no deja foto
        falloBD = true;
        try {
            servicio.revertirEstado(1);
            assert false : "debía fallar";
        } catch (RuntimeException esperado) {
            assert "ENVIADO".equals(BD.getEstado());
        }
        falloBD = false;
        servicio.revertirEstado(1);
        assert "EN_DESPACHO".equals(BD.getEstado());
        servicio.revertirEstado(1);
        assert historial.ultima(1) == null;
        falloBD = true;
        try {
            servicio.avanzarEstado(1);
            assert false : "debía fallar";
        } catch (RuntimeException esperado) {
            assert historial.ultima(1) == null;
        }
        falloBD = false;

        // pedido inexistente
        fallaConRegla(servicio, 99);
        System.out.println("OK");
    }

    private static void fallaConRegla(PedidoService servicio, int id) {
        try {
            servicio.revertirEstado(id);
            assert false : "debía rechazarse";
        } catch (ReglaNegocioException esperado) {
            // esperado
        }
    }
}
