package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.reservas;

import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservasActivasDeProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeProveedorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservasStubAdapterTest {

    @Mock
    private ContarReservasActivasDeClienteUseCase contarReservasActivasDeClienteUseCase;

    @Mock
    private ContarReservasActivasDeProveedorUseCase contarReservasActivasDeProveedorUseCase;

    @Mock
    private CancelarReservasActivasDeClienteUseCase cancelarReservasActivasDeClienteUseCase;

    @Mock
    private CancelarReservasActivasDeProveedorUseCase cancelarReservasActivasDeProveedorUseCase;

    private ReservasStubAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReservasStubAdapter(
                contarReservasActivasDeClienteUseCase,
                contarReservasActivasDeProveedorUseCase,
                cancelarReservasActivasDeClienteUseCase,
                cancelarReservasActivasDeProveedorUseCase
        );
    }

    @Test
    @DisplayName("contarReservasActivasDeCliente delega al caso de uso correspondiente")
    void contarReservasActivasDeCliente_delega() {
        when(contarReservasActivasDeClienteUseCase.ejecutar(10L)).thenReturn(3);

        int resultado = adapter.contarReservasActivasDeCliente(10L);

        assertEquals(3, resultado);
        verify(contarReservasActivasDeClienteUseCase).ejecutar(10L);
    }

    @Test
    @DisplayName("contarReservasActivasDeProveedor delega al caso de uso correspondiente")
    void contarReservasActivasDeProveedor_delega() {
        when(contarReservasActivasDeProveedorUseCase.ejecutar(20L)).thenReturn(5);

        int resultado = adapter.contarReservasActivasDeProveedor(20L);

        assertEquals(5, resultado);
        verify(contarReservasActivasDeProveedorUseCase).ejecutar(20L);
    }

    @Test
    @DisplayName("cancelarReservasActivasDeCliente delega al caso de uso correspondiente")
    void cancelarReservasActivasDeCliente_delega() {
        when(cancelarReservasActivasDeClienteUseCase.ejecutar(10L, "Motivo")).thenReturn(2);

        int resultado = adapter.cancelarReservasActivasDeCliente(10L, "Motivo");

        assertEquals(2, resultado);
        verify(cancelarReservasActivasDeClienteUseCase).ejecutar(10L, "Motivo");
    }

    @Test
    @DisplayName("cancelarReservasActivasDeProveedor delega al caso de uso correspondiente")
    void cancelarReservasActivasDeProveedor_delega() {
        when(cancelarReservasActivasDeProveedorUseCase.ejecutar(20L, "Motivo")).thenReturn(4);

        int resultado = adapter.cancelarReservasActivasDeProveedor(20L, "Motivo");

        assertEquals(4, resultado);
        verify(cancelarReservasActivasDeProveedorUseCase).ejecutar(20L, "Motivo");
    }
}
