package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesactivarRecursoServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private DesactivarRecursoService service;

    @Test
    @DisplayName("Desactiva el recurso y lo guarda")
    void deactivatesAndSaves() {
        // Arrange
        Recurso recurso = Recurso.nuevo(7, "Camilla", BigDecimal.TEN, 3);
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recurso));

        // Act
        service.ejecutar(1);

        // Assert
        assertFalse(recurso.estaActivo());
        verify(recursoRepositoryPort).guardar(recurso);
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(1));
        verify(recursoRepositoryPort, never()).guardar(any());
    }
}
