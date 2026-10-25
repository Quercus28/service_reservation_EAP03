package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
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
class ContarReservasActivasDeProveedorServiceTest {

    @Mock private ConsultarProveedorUseCase consultarProveedorUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;

    @InjectMocks private ContarReservasActivasDeProveedorService service;

    @Test
    @DisplayName("Cuenta las reservas activas del proveedor del usuario")
    void countsActiveReservations() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(30L)).thenReturn(Optional.of(7));
        when(reservaRepositoryPort.contarActivasDeProveedor(7)).thenReturn(2);

        assertEquals(2, service.ejecutar(30L));
    }

    @Test
    @DisplayName("Devuelve cero si el usuario no es proveedor o no se indica usuario")
    void returnsZeroWithoutProvider() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(30L)).thenReturn(Optional.empty());

        assertEquals(0, service.ejecutar(30L));
        assertEquals(0, service.ejecutar(null));
        verifyNoInteractions(reservaRepositoryPort);
    }
}
