package com.botica.dao;

import com.botica.model.Categoria;
import java.util.List;

public interface CategoriaDAO {
    void crear(Categoria c);
    Categoria buscarPorId(int id);
    Categoria buscarPorNombre(String nombre);
    List<Categoria> listarTodas();
    List<Categoria> listarActivas();
    void renombrar(int id, String nombre);
    void cambiarActiva(int id, boolean activa);
    void eliminar(int id);
    int contarProductos(int id);
}
