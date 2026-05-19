package com.udes.pedidos.service;

import com.udes.pedidos.domain.*;
import com.udes.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * PedidoService — VERSIÓN REFACTORIZADA
 *
 * Técnicas aplicadas:
 *  1. Value Objects: DatosCliente, Direccion, CodigoDescuento, LineaPedido
 *     → Elimina Primitive Obsession (12 parámetros primitivos → 4 objetos ricos)
 *  2. Extract Method: calcularTotal(), aplicarDescuento(), persistirPedido()
 *     → Reduce Long Method (CC original ~8 → CC procesarPedido = 1)
 *  3. Extract Class: NotificacionService
 *     → Elimina Large Class / SRP violation
 *  4. Inyección por constructor (no @Autowired en campo)
 *     → Mejora testabilidad y sigue buenas prácticas de Spring
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         NotificacionService notificacionService) {
        this.pedidoRepository = pedidoRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * procesarPedido — CC = 1, máximo 8 líneas de lógica.
     * Orquesta sin implementar: delega a métodos con responsabilidad única.
     */
    public String procesarPedido(Long clienteId,
                                  DatosCliente cliente,
                                  LineaPedido[] lineas,
                                  String metodoPago,
                                  boolean esUrgente,
                                  CodigoDescuento descuento) {
        double total = calcularTotal(lineas);
        double totalConDescuento = aplicarDescuento(total, descuento);
        notificacionService.notificarPedido(cliente, esUrgente);
        return persistirPedido(clienteId, cliente, totalConDescuento);
    }

    // ── Extract Method ────────────────────────────────────────────────────────

    /** CC = 1: calcula el total sumando subtotales de cada línea. */
    private double calcularTotal(LineaPedido[] lineas) {
        return Arrays.stream(lineas)
                .mapToDouble(LineaPedido::getSubtotal)
                .sum();
    }

    /** CC = 2: aplica el descuento si el código es válido. */
    private double aplicarDescuento(double total, CodigoDescuento descuento) {
        if (descuento == null || !descuento.esValido()) return total;
        return total * (1 - descuento.getPorcentaje());
    }

    /** CC = 1: persiste el pedido y retorna el identificador. */
    private String persistirPedido(Long clienteId, DatosCliente cliente, double total) {
        Pedido pedido = new Pedido(clienteId, cliente.getNombre(), total);
        return "OK_" + pedidoRepository.save(pedido).getId();
    }
}
