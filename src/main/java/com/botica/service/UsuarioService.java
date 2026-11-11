package com.botica.service;

import com.botica.dao.UsuarioDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Usuario;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import com.botica.util.Seguridad;

/** Reglas de negocio de usuarios: registro, autenticación, control de duplicados y gestión de roles. */
public class UsuarioService {
    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(DAOFactory.getUsuarioDAO());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /** Registro público: siempre crea cuentas de cliente. */
    public Usuario registrarCliente(String nombre, String correo, String contrasena) {
        return crear(nombre, correo, contrasena, Constantes.ROL_CLIENTE);
    }

    /** Alta de personal hecha por el Administrador. */
    public Usuario crearPersonal(String nombre, String correo, String contrasena, String rol) {
        if (!Constantes.ROLES.contains(rol)) {
            throw new ReglaNegocioException("Rol inválido");
        }
        return crear(nombre, correo, contrasena, rol);
    }

    public Usuario autenticar(String correo, String contrasena) {
        Usuario usuario = (correo == null || correo.isBlank())
                ? null : usuarioDAO.buscarPorCorreo(correo.trim().toLowerCase());
        if (usuario == null || contrasena == null || !Seguridad.verificar(contrasena, usuario.getContrasena())) {
            throw new ReglaNegocioException("Correo o contraseña incorrectos");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("Tu cuenta está desactivada. Contacta al administrador");
        }
        return usuario;
    }

    /** El administrador activa o desactiva cuentas (una cuenta desactivada no puede iniciar sesión). */
    public void cambiarActivo(int idActor, int idDestino, boolean activo) {
        Usuario destino = usuarioDAO.buscarPorId(idDestino);
        if (destino == null) {
            throw new ReglaNegocioException("Usuario no encontrado");
        }
        if (destino.getId() == idActor) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuarioDAO.actualizarActivo(idDestino, activo);
    }

    /** El administrador asigna una contraseña nueva al PERSONAL (no a clientes); se guarda siempre con hash. */
    public void resetearContrasena(int idDestino, String nueva) {
        if (nueva == null || nueva.length() < Constantes.LONGITUD_MINIMA_CONTRASENA) {
            throw new ReglaNegocioException("La contraseña debe tener al menos "
                    + Constantes.LONGITUD_MINIMA_CONTRASENA + " caracteres");
        }
        Usuario destino = usuarioDAO.buscarPorId(idDestino);
        if (destino == null) {
            throw new ReglaNegocioException("Usuario no encontrado");
        }
        if (Constantes.ROL_CLIENTE.equals(destino.getRol())) {
            throw new ReglaNegocioException("La contraseña de un cliente no se restablece desde el panel: cada cliente administra la suya");
        }
        usuarioDAO.actualizarContrasena(idDestino, Seguridad.hashear(nueva));
    }

    public void cambiarRol(int idActor, int idDestino, String nuevoRol) {
        Usuario destino = usuarioDAO.buscarPorId(idDestino);
        if (destino == null || !Constantes.ROLES.contains(nuevoRol)) {
            throw new ReglaNegocioException("Datos inválidos");
        }
        if (destino.getId() == idActor) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol");
        }
        usuarioDAO.actualizarRol(idDestino, nuevoRol);
    }

    private Usuario crear(String nombre, String correo, String contrasena, String rol) {
        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()
                || contrasena == null || contrasena.length() < Constantes.LONGITUD_MINIMA_CONTRASENA) {
            throw new ReglaNegocioException("Completa todos los campos (la contraseña debe tener al menos "
                    + Constantes.LONGITUD_MINIMA_CONTRASENA + " caracteres)");
        }
        String correoNormalizado = correo.trim().toLowerCase();
        if (usuarioDAO.buscarPorCorreo(correoNormalizado) != null) {
            throw new ReglaNegocioException("Ese correo ya está registrado");
        }
        Usuario usuario = new Usuario(nombre.trim(), correoNormalizado, Seguridad.hashear(contrasena), rol);
        usuarioDAO.crear(usuario);
        return usuario;
    }
}
