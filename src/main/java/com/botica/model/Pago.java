package com.botica.model;

import java.time.LocalDateTime;

/** Registro de un pago realizado por el cliente para un pedido. */
public class Pago {
    private int id;
    private int idPedido;
    private String metodo;
    private double monto;
    private String referencia;
    private LocalDateTime fecha;

    public Pago() {
    }

    public Pago(int idPedido, String metodo, double monto, String referencia) {
        this.idPedido = idPedido;
        this.metodo = metodo;
        this.monto = monto;
        this.referencia = referencia;
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

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
