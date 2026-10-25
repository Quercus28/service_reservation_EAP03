package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContarReservasActivasDeClienteServiceTest {

    @Mock private ConsultarClienteUseCase consultarClienteUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;

    @InjectMocks private ContarReservasActivasDeClienteService service;

    @Test
    @DisplayName("Cuenta las reservas activas del cliente del usuario")
    void countsActiveReservations() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.contarActivasDeCliente(3L)).thenReturn(4);

        assertEquals(4, service.ejecutar(20L));
    }

    @Test
    @DisplayName("Devuelve cero si el usuario no es cliente o no se indica usuario")
    void returnsZeroWithoutClient() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.empty());

        assertEquals(0, service.ejecutar(20L));
        assertEquals(0, service.ejecutar(null));
        verifyNoInteractions(reservaRepositoryPort);
    }
}
