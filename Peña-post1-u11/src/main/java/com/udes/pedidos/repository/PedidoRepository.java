package com.udes.pedidos.repository;

import com.udes.pedidos.domain.Pedido;
import com.udes.pedidos.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Metodo auxiliar para buscar producto por id (simulado con JPA)
    default Producto findProductoById(Long id) { return null; }
}
