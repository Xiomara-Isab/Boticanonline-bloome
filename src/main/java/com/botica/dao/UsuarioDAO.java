package com.botica.dao;

import com.botica.model.Usuario;
import java.util.List;

public interface UsuarioDAO {
    void crear(Usuario u);
    Usuario buscarPorCorreo(String correo);
    Usuario buscarPorId(int id);
    List<Usuario> listarTodos();
    void actualizarRol(int id, String rol);
    void actualizarActivo(int id, boolean activo);
    void actualizarContrasena(int id, String contrasenaHasheada);
}
