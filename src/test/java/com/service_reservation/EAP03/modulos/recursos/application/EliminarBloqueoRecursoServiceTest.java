package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarBloqueoRecursoServiceTest {

    @Mock private BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;

    @InjectMocks private EliminarBloqueoRecursoService service;

    private static BloqueoRecurso blockOf(int idRecurso) {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 8, 0);
        return BloqueoRecurso.reconstruir(8, idRecurso, inicio, inicio.plusHours(2), null, inicio.minusDays(1));
    }

    @Test
    @DisplayName("Elimina el bloqueo cuando pertenece al recurso")
    void deletesOwnBlock() {
        when(bloqueoRecursoRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(blockOf(1)));

        service.ejecutar(1, 8);

        verify(bloqueoRecursoRepositoryPort).eliminar(8);
    }

    @Test
    @DisplayName("Un bloqueo de otro recurso se trata como inexistente")
    void blockOfAnotherResourceIsNotFound() {
        when(bloqueoRecursoRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(blockOf(2)));

        assertThrows(BloqueoNoEncontradoException.class, () -> service.ejecutar(1, 8));
        verify(bloqueoRecursoRepositoryPort, never()).eliminar(any());
    }

    @Test
    @DisplayName("Falla si el bloqueo no existe")
    void unknownBlock() {
        when(bloqueoRecursoRepositoryPort.buscarPorId(8)).thenReturn(Optional.empty());

        assertThrows(BloqueoNoEncontradoException.class, () -> service.ejecutar(1, 8));
    }
}
