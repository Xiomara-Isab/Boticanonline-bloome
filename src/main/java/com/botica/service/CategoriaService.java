package com.botica.service;

import com.botica.dao.CategoriaDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Categoria;
import com.botica.util.ReglaNegocioException;
import java.util.List;

/**
 * Reglas de negocio de las categorías del catálogo: nombre obligatorio y sin duplicados,
 * y una categoría con productos no se elimina (integridad de los datos: ningún producto queda
 * apuntando a una categoría que no existe). Si no se usa más, se desactiva.
 */
public class CategoriaService {
    private static final int LONGITUD_MAXIMA = 50;
    private final CategoriaDAO categoriaDAO;

    public CategoriaService() {
        this(DAOFactory.getCategoriaDAO());
    }

    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public List<Categoria> listar() {
        return categoriaDAO.listarTodas();
    }

    /** Las que el vendedor puede asignar y el catálogo muestra como filtro. */
    public List<Categoria> listarActivas() {
        return categoriaDAO.listarActivas();
    }

    public void crear(String nombre) {
        String limpio = validarNombre(nombre, null);
        categoriaDAO.crear(new Categoria(limpio));
    }

    public void renombrar(int id, String nombre) {
        Categoria categoria = obtener(id);
        String limpio = validarNombre(nombre, categoria.getId());
        categoriaDAO.renombrar(id, limpio);
    }

    public void cambiarActiva(int id, boolean activa) {
        obtener(id);
        categoriaDAO.cambiarActiva(id, activa);
    }

    public void eliminar(int id) {
        Categoria categoria = obtener(id);
        int enUso = categoriaDAO.contarProductos(id);
        if (enUso > 0) {
            throw new ReglaNegocioException("No se puede eliminar \"" + categoria.getNombre() + "\": tiene " + enUso
                    + " producto(s). Cámbialos de categoría o desactívala para que no se use en productos nuevos");
        }
        try {
            categoriaDAO.eliminar(id);
        } catch (RuntimeException e) {
            // la clave foránea lo impide si alguien le asignó un producto justo ahora
            throw new ReglaNegocioException("No se puede eliminar \"" + categoria.getNombre() + "\": está en uso");
        }
    }

    private Categoria obtener(int id) {
        Categoria categoria = categoriaDAO.buscarPorId(id);
        if (categoria == null) {
            throw new ReglaNegocioException("Categoría no encontrada");
        }
        return categoria;
    }

    /** Devuelve el nombre sin espacios sobrantes; idPropio es la categoría que se renombra (null al crear). */
    private String validarNombre(String nombre, Integer idPropio) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio");
        }
        if (limpio.length() > LONGITUD_MAXIMA) {
            throw new ReglaNegocioException("El nombre admite como máximo " + LONGITUD_MAXIMA + " caracteres");
        }
        Categoria existente = categoriaDAO.buscarPorNombre(limpio);
        if (existente != null && (idPropio == null || existente.getId() != idPropio)) {
            throw new ReglaNegocioException("Ya existe la categoría \"" + existente.getNombre() + "\"");
        }
        return limpio;
    }
}
