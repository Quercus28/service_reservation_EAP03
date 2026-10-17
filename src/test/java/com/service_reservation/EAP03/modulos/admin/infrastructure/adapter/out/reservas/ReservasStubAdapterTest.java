package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.reservas;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservasStubAdapterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ConsultarClienteUseCase consultarClienteUseCase;

    @Mock
    private ConsultarProveedorUseCase consultarProveedorUseCase;

    private Clock clock;
    private ReservasStubAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneId.of("UTC"));
        adapter = new ReservasStubAdapter(jdbcTemplate, consultarClienteUseCase, consultarProveedorUseCase, clock);
    }

    @Test
    @DisplayName("contarReservasActivasDeCliente retorna 0 si idUsuario es null")
    void contarReservasActivasDeCliente_idUsuarioNull() {
        assertEquals(0, adapter.contarReservasActivasDeCliente(null));
        verifyNoInteractions(consultarClienteUseCase, jdbcTemplate);
    }

    @Test
    @DisplayName("contarReservasActivasDeCliente retorna 0 si no existe perfil de cliente")
    void contarReservasActivasDeCliente_sinPerfil() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(10L)).thenReturn(Optional.empty());

        assertEquals(0, adapter.contarReservasActivasDeCliente(10L));
        verify(consultarClienteUseCase).buscarIdClientePorUsuario(10L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("contarReservasActivasDeCliente consulta y retorna la cantidad de reservas activas")
    void contarReservasActivasDeCliente_exitoso() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(10L)).thenReturn(Optional.of(100L));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(100L))).thenReturn(3);

        assertEquals(3, adapter.contarReservasActivasDeCliente(10L));
        verify(jdbcTemplate).queryForObject(contains("WHERE id_cliente = ?"), eq(Integer.class), eq(100L));
    }

    @Test
    @DisplayName("contarReservasActivasDeProveedor retorna 0 si idUsuario es null")
    void contarReservasActivasDeProveedor_idUsuarioNull() {
        assertEquals(0, adapter.contarReservasActivasDeProveedor(null));
        verifyNoInteractions(consultarProveedorUseCase, jdbcTemplate);
    }

    @Test
    @DisplayName("contarReservasActivasDeProveedor retorna 0 si no existe perfil de proveedor")
    void contarReservasActivasDeProveedor_sinPerfil() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.empty());

        assertEquals(0, adapter.contarReservasActivasDeProveedor(20L));
        verify(consultarProveedorUseCase).buscarIdProveedorPorUsuario(20L);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("contarReservasActivasDeProveedor consulta y retorna la cantidad de reservas activas")
    void contarReservasActivasDeProveedor_exitoso() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.of(200));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(200))).thenReturn(5);

        assertEquals(5, adapter.contarReservasActivasDeProveedor(20L));
        verify(jdbcTemplate).queryForObject(contains("WHERE id_proveedor = ?"), eq(Integer.class), eq(200));
    }

    @Test
    @DisplayName("cancelarReservasActivasDeCliente cancela y retorna filas actualizadas")
    void cancelarReservasActivasDeCliente_exitoso() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(10L)).thenReturn(Optional.of(100L));
        when(jdbcTemplate.update(anyString(), any(LocalDateTime.class), eq("Motivo sanción"), eq(100L))).thenReturn(2);

        int canceladas = adapter.cancelarReservasActivasDeCliente(10L, "Motivo sanción");

        assertEquals(2, canceladas);
        verify(jdbcTemplate).update(
                contains("SET estado = 'CANCELADO'"),
                eq(LocalDateTime.now(clock)),
                eq("Motivo sanción"),
                eq(100L)
        );
    }

    @Test
    @DisplayName("cancelarReservasActivasDeCliente retorna 0 si idUsuario es null o sin perfil")
    void cancelarReservasActivasDeCliente_invalido() {
        assertEquals(0, adapter.cancelarReservasActivasDeCliente(null, "Motivo"));
        when(consultarClienteUseCase.buscarIdClientePorUsuario(10L)).thenReturn(Optional.empty());
        assertEquals(0, adapter.cancelarReservasActivasDeCliente(10L, "Motivo"));
    }

    @Test
    @DisplayName("cancelarReservasActivasDeProveedor cancela y trunca motivo largo a 255 caracteres")
    void cancelarReservasActivasDeProveedor_truncaMotivo() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.of(200));
        String motivoLargo = "A".repeat(300);
        when(jdbcTemplate.update(anyString(), any(LocalDateTime.class), eq("A".repeat(255)), eq(200))).thenReturn(4);

        int canceladas = adapter.cancelarReservasActivasDeProveedor(20L, motivoLargo);

        assertEquals(4, canceladas);
        verify(jdbcTemplate).update(
                contains("SET estado = 'CANCELADO'"),
                eq(LocalDateTime.now(clock)),
                eq("A".repeat(255)),
                eq(200)
        );
    }
}
