package com.botica.service;

import com.botica.dao.CategoriaDAO;
import com.botica.model.Categoria;
import com.botica.util.ReglaNegocioException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprobación sin base de datos de las reglas de categorías (nombre vacío, duplicados y categorías en uso).
 * Ejecutar con: java -ea -cp "target/classes:target/test-classes" com.botica.service.CategoriaServiceCheck
 */
public class CategoriaServiceCheck {
    private static final Map<Integer, Categoria> BD = new LinkedHashMap<>();
    private static int siguienteId = 1;

    private static final CategoriaDAO DAO = new CategoriaDAO() {
        @Override public void crear(Categoria c) { c.setId(siguienteId++); BD.put(c.getId(), c); }
        @Override public Categoria buscarPorId(int id) { return BD.get(id); }
        @Override public Categoria buscarPorNombre(String nombre) {
            for (Categoria c : BD.values()) if (c.getNombre().equalsIgnoreCase(nombre.trim())) return c;
            return null;
        }
        @Override public List<Categoria> listarTodas() { return new ArrayList<>(BD.values()); }
        @Override public List<Categoria> listarActivas() {
            List<Categoria> activas = new ArrayList<>();
            for (Categoria c : BD.values()) if (c.isActiva()) activas.add(c);
            return activas;
        }
        @Override public void renombrar(int id, String nombre) { BD.get(id).setNombre(nombre); }
        @Override public void cambiarActiva(int id, boolean activa) { BD.get(id).setActiva(activa); }
        @Override public void eliminar(int id) { BD.remove(id); }
        @Override public int contarProductos(int id) { return BD.get(id).getCantidadProductos(); }
    };

    public static void main(String[] args) {
        CategoriaService servicio = new CategoriaService(DAO);

        // crear: limpia espacios y rechaza vacíos, muy largos y duplicados (sin importar mayúsculas)
        servicio.crear("  Higiene   bucal ");
        assert BD.get(1).getNombre().equals("Higiene bucal");
        servicio.crear("Vitaminas");
        rechaza(() -> servicio.crear("   "));
        rechaza(() -> servicio.crear(null));
        rechaza(() -> servicio.crear("x".repeat(51)));
        rechaza(() -> servicio.crear("vitaminas"));
        assert BD.size() == 2 : "un intento rechazado no debe crear nada";

        // renombrar: puede quedarse con su propio nombre, pero no tomar el de otra
        servicio.renombrar(1, "Higiene Bucal");
        assert BD.get(1).getNombre().equals("Higiene Bucal");
        rechaza(() -> servicio.renombrar(1, "VITAMINAS"));
        rechaza(() -> servicio.renombrar(99, "Otra"));

        // activar/desactivar: las inactivas no se ofrecen para productos nuevos
        servicio.cambiarActiva(2, false);
        assert servicio.listarActivas().size() == 1;
        servicio.cambiarActiva(2, true);

        // eliminar: una categoría con productos no se borra; una sin productos sí
        BD.get(2).setCantidadProductos(3);
        rechaza(() -> servicio.eliminar(2));
        assert BD.containsKey(2) : "una categoría en uso no debe borrarse";
        servicio.eliminar(1);
        assert !BD.containsKey(1);
        rechaza(() -> servicio.eliminar(1));
        System.out.println("OK");
    }

    private static void rechaza(Runnable accion) {
        try {
            accion.run();
            assert false : "debía rechazarse";
        } catch (ReglaNegocioException esperado) {
            // correcto
        }
    }
}
