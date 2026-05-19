package com.udes.pedidos.domain;

import java.util.Objects;

/**
 * Value Object: DatosCliente
 * Elimina Primitive Obsession agrupando los 6 parámetros primitivos del cliente.
 * Inmutable: campos final, sin setters, validación en constructor.
 */
public final class DatosCliente {

    private final String nombre;
    private final String email;
    private final String telefono;
    private final Direccion direccion;

    public DatosCliente(String nombre, String email,
                        String telefono, Direccion direccion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre requerido");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email invalido");
        if (direccion == null)
            throw new IllegalArgumentException("Direccion requerida");
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public Direccion getDireccion() { return direccion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatosCliente dc)) return false;
        return Objects.equals(email, dc.email)
            && Objects.equals(nombre, dc.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, email);
    }

    @Override
    public String toString() {
        return nombre + " <" + email + ">";
    }
}
