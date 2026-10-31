package com.botica.model;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private int id;
    private int idCliente;
    private LocalDateTime fechaPedido;
    private String estado;
    private double total;

    public Pedido() {
    }

    public Pedido(int idCliente, String estado, double total) {
        this.idCliente = idCliente;
        this.estado = estado;
        this.total = total;
    }

    public void calcularTotal(List<DetallePedido> detalles) {
        double suma = 0;
        for (DetallePedido d : detalles) {
            suma += d.getSubtotal();
        }
        this.total = suma;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
