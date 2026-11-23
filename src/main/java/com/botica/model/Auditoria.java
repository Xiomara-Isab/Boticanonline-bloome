package com.botica.model;

import java.time.LocalDateTime;

/** Registro de la bitácora: quién hizo qué, sobre qué entidad, desde qué IP y cuándo. */
public class Auditoria {
    private int id;
    private Integer idUsuario;
    private String rol;
    private String accion;
    private String entidad;
    private Integer idEntidad;
    private String detalle;
    private String ip;
    private LocalDateTime fecha;
    private String correoUsuario; // solo lectura (JOIN con usuario)

    public Auditoria() {
    }

    public Auditoria(Integer idUsuario, String rol, String accion, String entidad, Integer idEntidad,
                     String detalle, String ip) {
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.accion = accion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.detalle = detalle;
        this.ip = ip;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public Integer getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Integer idEntidad) { this.idEntidad = idEntidad; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }
}
