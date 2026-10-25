package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.AsociacionNoEncontradaException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuitarRecursoDeServicioServiceTest {

    @Mock private ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    @InjectMocks private QuitarRecursoDeServicioService service;

    @Test
    @DisplayName("Elimina la asociación entre el servicio y el recurso")
    void removesLink() {
        when(servicioRecursoRepositoryPort.buscarPorServicioYRecurso(10, 3)).thenReturn(Optional.of(ServicioRecurso.reconstruir(4, 10, 3, 1)));

        service.ejecutar(10, 3);

        verify(servicioRecursoRepositoryPort).eliminar(4);
    }

    @Test
    @DisplayName("Falla si el recurso no está asociado al servicio")
    void unknownLink() {
        when(servicioRecursoRepositoryPort.buscarPorServicioYRecurso(10, 3)).thenReturn(Optional.empty());

        assertThrows(AsociacionNoEncontradaException.class, () -> service.ejecutar(10, 3));
        verify(servicioRecursoRepositoryPort, never()).eliminar(any());
    }
}
