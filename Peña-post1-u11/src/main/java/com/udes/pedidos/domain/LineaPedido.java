package com.udes.pedidos.domain;

import java.util.Objects;

/**
 * Value Object: LineaPedido
 * Representa una línea de un pedido (producto + cantidad).
 * Encapsula el cálculo del subtotal.
 */
public final class LineaPedido {

    private final Long productoId;
    private final double precioUnitario;
    private final int cantidad;

    public LineaPedido(Long productoId, double precioUnitario, int cantidad) {
        if (productoId == null)
            throw new IllegalArgumentException("ProductoId requerido");
        if (precioUnitario < 0)
            throw new IllegalArgumentException("Precio no puede ser negativo");
        if (cantidad <= 0)
            throw new IllegalArgumentException("Cantidad debe ser mayor a cero");
        this.productoId = productoId;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public Long getProductoId() { return productoId; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public double getSubtotal() { return precioUnitario * cantidad; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaPedido lp)) return false;
        return Objects.equals(productoId, lp.productoId) && cantidad == lp.cantidad;
    }

    @Override
    public int hashCode() { return Objects.hash(productoId, cantidad); }
}
