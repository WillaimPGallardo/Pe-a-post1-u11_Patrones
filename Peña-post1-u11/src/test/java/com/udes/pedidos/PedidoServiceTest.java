package com.udes.pedidos;

import com.udes.pedidos.domain.*;
import com.udes.pedidos.repository.PedidoRepository;
import com.udes.pedidos.service.NotificacionService;
import com.udes.pedidos.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private NotificacionService notificacionService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepository, notificacionService);
    }

    // ── Value Object: Direccion ───────────────────────────────────────────────

    @Test
    @DisplayName("Direccion: constructor valida campos requeridos")
    void direccion_debeLanzarExcepcionSiCalleEsNula() {
        assertThatThrownBy(() -> new Direccion(null, "Cucuta", "540001"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("calle");
    }

    @Test
    @DisplayName("Direccion: equals funciona correctamente")
    void direccion_equalsDebeFuncionarCorrectamente() {
        Direccion d1 = new Direccion("Calle 5", "Cucuta", "540001");
        Direccion d2 = new Direccion("Calle 5", "Cucuta", "540001");
        assertThat(d1).isEqualTo(d2);
    }

    // ── Value Object: DatosCliente ────────────────────────────────────────────

    @Test
    @DisplayName("DatosCliente: email invalido lanza excepcion")
    void datosCliente_debeLanzarExcepcionSiEmailEsInvalido() {
        Direccion dir = new Direccion("Calle 5", "Cucuta", "540001");
        assertThatThrownBy(() -> new DatosCliente("Juan", "emailSinArroba", "3001234567", dir))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("DatosCliente: nombre en blanco lanza excepcion")
    void datosCliente_debeLanzarExcepcionSiNombreEsBlanco() {
        Direccion dir = new Direccion("Calle 5", "Cucuta", "540001");
        assertThatThrownBy(() -> new DatosCliente("   ", "juan@test.com", "3001234567", dir))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nombre");
    }

    @Test
    @DisplayName("DatosCliente: construccion exitosa con datos validos")
    void datosCliente_construccionExitosaConDatosValidos() {
        Direccion dir = new Direccion("Calle 5", "Cucuta", "540001");
        DatosCliente cliente = new DatosCliente("Juan Perez", "juan@test.com", "3001234567", dir);
        assertThat(cliente.getNombre()).isEqualTo("Juan Perez");
        assertThat(cliente.getEmail()).isEqualTo("juan@test.com");
    }

    // ── Value Object: CodigoDescuento ─────────────────────────────────────────

    @Test
    @DisplayName("CodigoDescuento: VIP10 aplica 10% de descuento")
    void codigoDescuento_vip10DebeAplicar10PorCiento() {
        CodigoDescuento descuento = CodigoDescuento.of("VIP10");
        assertThat(descuento).isNotNull();
        assertThat(descuento.getPorcentaje()).isEqualTo(0.10);
        assertThat(descuento.esValido()).isTrue();
    }

    @Test
    @DisplayName("CodigoDescuento: NEW20 aplica 20% de descuento")
    void codigoDescuento_new20DebeAplicar20PorCiento() {
        CodigoDescuento descuento = CodigoDescuento.of("NEW20");
        assertThat(descuento.getPorcentaje()).isEqualTo(0.20);
    }

    @Test
    @DisplayName("CodigoDescuento: codigo invalido retorna porcentaje 0")
    void codigoDescuento_codigoInvalidoNoAplicaDescuento() {
        CodigoDescuento descuento = CodigoDescuento.of("INVALIDO");
        assertThat(descuento.esValido()).isFalse();
    }

    @Test
    @DisplayName("CodigoDescuento: null retorna null")
    void codigoDescuento_nullRetornaNull() {
        assertThat(CodigoDescuento.of(null)).isNull();
    }

    // ── Value Object: LineaPedido ─────────────────────────────────────────────

    @Test
    @DisplayName("LineaPedido: subtotal calculado correctamente")
    void lineaPedido_subtotalDebeCalcularseCorrectamente() {
        LineaPedido linea = new LineaPedido(1L, 50.0, 3);
        assertThat(linea.getSubtotal()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("LineaPedido: cantidad cero lanza excepcion")
    void lineaPedido_cantidadCeroLanzaExcepcion() {
        assertThatThrownBy(() -> new LineaPedido(1L, 50.0, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── PedidoService: flujo completo ─────────────────────────────────────────

    @Test
    @DisplayName("procesarPedido: retorna OK_ con id del pedido guardado")
    void procesarPedido_debeRetornarOkConId() {
        Pedido pedidoGuardado = new Pedido(1L, "Juan Perez", 90.0);
        // Usamos reflexion para setear el id
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        Direccion dir = new Direccion("Calle 5", "Cucuta", "540001");
        DatosCliente cliente = new DatosCliente("Juan Perez", "juan@test.com", "300", dir);
        LineaPedido[] lineas = { new LineaPedido(1L, 100.0, 1) };
        CodigoDescuento descuento = CodigoDescuento.of("VIP10");

        String resultado = pedidoService.procesarPedido(1L, cliente, lineas, "EFECTIVO", false, descuento);

        assertThat(resultado).startsWith("OK_");
        verify(notificacionService).notificarPedido(cliente, false);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("procesarPedido: sin descuento usa total completo")
    void procesarPedido_sinDescuentoUsaTotalCompleto() {
        Pedido pedidoGuardado = new Pedido(1L, "Maria Lopez", 200.0);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        Direccion dir = new Direccion("Av. 1", "Bogota", "110001");
        DatosCliente cliente = new DatosCliente("Maria Lopez", "maria@test.com", "311", dir);
        LineaPedido[] lineas = { new LineaPedido(2L, 100.0, 2) };

        String resultado = pedidoService.procesarPedido(2L, cliente, lineas, "TARJETA", true, null);

        assertThat(resultado).startsWith("OK_");
        verify(notificacionService).notificarPedido(cliente, true);
    }

    @Test
    @DisplayName("procesarPedido: pedido urgente notifica con flag urgente=true")
    void procesarPedido_pedidoUrgenteNotificaCorrectamente() {
        Pedido p = new Pedido(3L, "Carlos", 50.0);
        when(pedidoRepository.save(any())).thenReturn(p);

        Direccion dir = new Direccion("Cra 8", "Medellin", "050001");
        DatosCliente cliente = new DatosCliente("Carlos", "carlos@test.com", "315", dir);
        LineaPedido[] lineas = { new LineaPedido(3L, 50.0, 1) };

        pedidoService.procesarPedido(3L, cliente, lineas, "PSE", true, null);

        verify(notificacionService).notificarPedido(cliente, true);
    }
}
