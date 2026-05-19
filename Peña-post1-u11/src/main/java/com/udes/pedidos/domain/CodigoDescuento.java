package com.udes.pedidos.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Value Object: CodigoDescuento
 * Elimina magic strings y lógica de descuento dispersa.
 * Encapsula la regla de negocio del porcentaje de descuento.
 */
public final class CodigoDescuento {

    private static final Map<String, Double> DESCUENTOS = Map.of(
        "VIP10", 0.10,
        "NEW20", 0.20
    );

    private final String codigo;
    private final double porcentaje;

    public static CodigoDescuento of(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        double porcentaje = DESCUENTOS.getOrDefault(codigo.toUpperCase(), 0.0);
        return new CodigoDescuento(codigo.toUpperCase(), porcentaje);
    }

    private CodigoDescuento(String codigo, double porcentaje) {
        this.codigo = codigo;
        this.porcentaje = porcentaje;
    }

    public String getCodigo() { return codigo; }
    public double getPorcentaje() { return porcentaje; }
    public boolean esValido() { return porcentaje > 0.0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoDescuento cd)) return false;
        return Objects.equals(codigo, cd.codigo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    @Override
    public String toString() { return codigo + "(-" + (int)(porcentaje * 100) + "%)"; }
}
