package com.udes.pedidos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clienteId;
    private String clienteNombre;
    private double total;

    public Pedido() {}

    public Pedido(Long clienteId, String clienteNombre, double total) {
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.total = total;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public double getTotal() { return total; }
}
