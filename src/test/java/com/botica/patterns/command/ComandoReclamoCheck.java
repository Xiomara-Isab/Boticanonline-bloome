package com.botica.patterns.command;

import com.botica.dao.ReclamoDAO;
import com.botica.model.Reclamo;
import com.botica.service.ReclamoService;
import com.botica.util.ReglaNegocioException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprobación sin base de datos del deshacer de reclamos. Ejecutar con:
 * mvn -q test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt &&
 * java -ea -cp "target/classes:target/test-classes:$(cat target/cp.txt)" com.botica.patterns.command.ComandoReclamoCheck
 */
public class ComandoReclamoCheck {
    private static final Map<Integer, Reclamo> BD = new HashMap<>();

    private static final ReclamoDAO DAO = new ReclamoDAO() {
        @Override public void crear(Reclamo r) { }
        @Override public Reclamo buscarPorId(int id) { return BD.get(id); }
        @Override public List<Reclamo> listarTodos() { return List.copyOf(BD.values()); }
        @Override public List<Reclamo> listarPorCliente(int idCliente) { return List.of(); }
        @Override public void actualizarEstado(int id, String estado) { BD.get(id).setEstado(estado); }
    };

    public static void main(String[] args) {
        Reclamo r = new Reclamo(1, "demo", "ABIERTO");
        BD.put(1, r);
        ReclamoService servicio = new ReclamoService(DAO, null);
        HistorialComandos historial = new HistorialComandos();

        // deshace en orden inverso, de uno en uno
        servicio.cambiarEstado(1, "EN_PROCESO", historial);
        servicio.cambiarEstado(1, "RESUELTO", historial);
        assert "reclamo #1 de EN PROCESO a RESUELTO".equals(historial.ultimaDescripcion());
        historial.deshacer();
        assert "EN_PROCESO".equals(r.getEstado());
        historial.deshacer();
        assert "ABIERTO".equals(r.getEstado());
        assert historial.ultimaDescripcion() == null;

        // guardar el mismo estado no ocupa un lugar en el historial
        servicio.cambiarEstado(1, "ABIERTO", historial);
        assert historial.ultimaDescripcion() == null;

        // no pisa el cambio de otro usuario, y el comando vencido no bloquea la pila
        servicio.cambiarEstado(1, "EN_PROCESO", historial);
        servicio.cambiarEstado(1, "ESCALADO", historial);
        r.setEstado("RESUELTO");
        fallaConRegla(historial);
        assert "RESUELTO".equals(r.getEstado());
        assert "reclamo #1 de ABIERTO a EN PROCESO".equals(historial.ultimaDescripcion());

        // sin nada que deshacer, y datos inválidos
        fallaConRegla(new HistorialComandos());
        try {
            servicio.cambiarEstado(99, "ABIERTO", historial);
            assert false : "el reclamo 99 no existe";
        } catch (ReglaNegocioException esperado) {
            System.out.println("OK");
        }
    }

    private static void fallaConRegla(HistorialComandos historial) {
        try {
            historial.deshacer();
            assert false : "debía rechazarse";
        } catch (ReglaNegocioException esperado) {
            // esperado
        }
    }
}
