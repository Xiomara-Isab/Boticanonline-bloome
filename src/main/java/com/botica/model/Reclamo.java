package com.botica.model;

import java.time.LocalDateTime;

public class Reclamo {
    private int id;
    private int idPedido;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaCreacion;
    private String respuesta;
    private int idSoporte;

    public Reclamo() {
    }

    public Reclamo(int idPedido, String descripcion, String estado) {
        this.idPedido = idPedido;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    /** Id del usuario de Soporte que atiende el reclamo (0 si nadie lo tomó). */
    public int getIdSoporte() {
        return idSoporte;
    }

    public void setIdSoporte(int idSoporte) {
        this.idSoporte = idSoporte;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
