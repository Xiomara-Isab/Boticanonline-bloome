package com.botica.service;

import com.botica.dao.AuditoriaDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Auditoria;
import com.botica.model.Usuario;
import com.botica.util.Html;
import java.time.LocalDate;
import java.util.List;

/**
 * Bitácora de auditoría. Quién y desde qué IP lo fija RolFilter por petición (ThreadLocal),
 * así los observadores y servicios pueden auditar sin recibir la petición HTTP.
 * Un fallo al auditar nunca corta la operación de negocio: solo se informa en el log.
 */
public class AuditoriaService {
    private record Contexto(Usuario usuario, String ip) {
    }

    private static final ThreadLocal<Contexto> CONTEXTO = new ThreadLocal<>();
    private final AuditoriaDAO auditoriaDAO;

    public AuditoriaService() {
        this(DAOFactory.getAuditoriaDAO());
    }

    public AuditoriaService(AuditoriaDAO auditoriaDAO) {
        this.auditoriaDAO = auditoriaDAO;
    }

    public static void fijarContexto(Usuario usuario, String ip) {
        CONTEXTO.set(new Contexto(usuario, ip));
    }

    public static void limpiarContexto() {
        CONTEXTO.remove();
    }

    /** Audita a nombre del usuario de la petición actual (puede no haber, p. ej. un login fallido). */
    public void registrar(String accion, String entidad, Integer idEntidad, String detalle) {
        Contexto c = CONTEXTO.get();
        registrar(c == null ? null : c.usuario(), accion, entidad, idEntidad, detalle);
    }

    public void registrar(Usuario usuario, String accion, String entidad, Integer idEntidad, String detalle) {
        Contexto c = CONTEXTO.get();
        try {
            auditoriaDAO.registrar(new Auditoria(usuario == null ? null : usuario.getId(),
                    usuario == null ? null : usuario.getRol(), accion, entidad, idEntidad, detalle,
                    c == null ? null : c.ip()));
        } catch (RuntimeException e) {
            System.err.println("No se pudo auditar " + accion + ": " + e.getMessage());
        }
    }

    public List<Auditoria> listar(LocalDate desde, LocalDate hasta, String usuario, String accion, int limite) {
        return auditoriaDAO.listar(desde, hasta, usuario, accion, limite);
    }

    public static String csv(List<Auditoria> registros) {
        StringBuilder sb = new StringBuilder("id,fecha,id_usuario,correo,rol,accion,entidad,id_entidad,detalle,ip\r\n");
        for (Auditoria a : registros) {
            sb.append(a.getId()).append(',')
                    .append(celda(Html.fecha(a.getFecha()))).append(',')
                    .append(a.getIdUsuario() == null ? "" : a.getIdUsuario()).append(',')
                    .append(celda(a.getCorreoUsuario())).append(',')
                    .append(celda(a.getRol())).append(',')
                    .append(celda(a.getAccion())).append(',')
                    .append(celda(a.getEntidad())).append(',')
                    .append(a.getIdEntidad() == null ? "" : a.getIdEntidad()).append(',')
                    .append(celda(a.getDetalle())).append(',')
                    .append(celda(a.getIp())).append("\r\n");
        }
        return sb.toString();
    }

    /** Entre comillas, y con ' delante si empieza como fórmula (el detalle puede traer texto del usuario). */
    private static String celda(String valor) {
        if (valor == null) return "";
        String v = valor.matches("^[=+\\-@\\t\\r].*") ? "'" + valor : valor;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
