package com.udes.pedidos.controller;

import com.udes.pedidos.domain.*;
import com.udes.pedidos.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<String> crearPedido(@RequestBody PedidoRequest request) {
        try {
            Direccion direccion = new Direccion(
                request.calle(), request.ciudad(), request.codigoPostal());

            DatosCliente cliente = new DatosCliente(
                request.nombre(), request.email(), request.telefono(), direccion);

            LineaPedido[] lineas = construirLineas(request.productosIds(), request.precios(), request.cantidades());

            CodigoDescuento descuento = CodigoDescuento.of(request.codigoDescuento());

            String resultado = pedidoService.procesarPedido(
                request.clienteId(), cliente, lineas,
                request.metodoPago(), request.esUrgente(), descuento);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
        }
    }

    private LineaPedido[] construirLineas(List<Long> ids, List<Double> precios, List<Integer> cantidades) {
        LineaPedido[] lineas = new LineaPedido[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            lineas[i] = new LineaPedido(ids.get(i), precios.get(i), cantidades.get(i));
        }
        return lineas;
    }

    public record PedidoRequest(
        Long clienteId, String nombre, String email, String telefono,
        String calle, String ciudad, String codigoPostal,
        List<Long> productosIds, List<Double> precios, List<Integer> cantidades,
        String metodoPago, boolean esUrgente, String codigoDescuento) {}
}
