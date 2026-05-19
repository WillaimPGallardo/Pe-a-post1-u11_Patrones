package com.udes.pedidos.service;

import com.udes.pedidos.domain.DatosCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Extract Class: NotificacionService
 * Responsabilidad única: gestionar notificaciones al cliente.
 * Extraído de PedidoService para respetar el Principio de Responsabilidad Única (SRP).
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void notificarPedido(DatosCliente cliente, boolean urgente) {
        enviarEmail(cliente);
        if (urgente) {
            enviarAlertaUrgente(cliente);
        }
    }

    private void enviarEmail(DatosCliente cliente) {
        log.info("Enviando confirmacion de pedido a: {}", cliente.getEmail());
    }

    private void enviarAlertaUrgente(DatosCliente cliente) {
        log.warn("PEDIDO URGENTE para: {} - Tel: {}", cliente.getNombre(), cliente.getTelefono());
    }
}
