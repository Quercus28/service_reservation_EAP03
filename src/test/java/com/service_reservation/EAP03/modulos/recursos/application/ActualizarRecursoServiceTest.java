package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDuplicadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ActualizarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualizarRecursoServiceTest {

    private static final ActualizarRecursoComando COMANDO =
            new ActualizarRecursoComando(1, "Camilla grande", new BigDecimal("70000.00"), 5);

    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private ActualizarRecursoService service;

    private static Recurso resource(LocalDateTime eliminadoEn) {
        return Recurso.reconstruir(1, 7, "Camilla", new BigDecimal("50000.00"), 3,
                LocalDateTime.of(2026, 9, 1, 8, 0), eliminadoEn);
    }

    @Test
    @DisplayName("Actualiza nombre, precio y stock y guarda el recurso")
    void updatesAndSaves() {
        // Arrange
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(resource(null)));
        when(recursoRepositoryPort.existeNombreActivo(7, "Camilla grande", 1)).thenReturn(false);
        when(recursoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Recurso actualizado = service.ejecutar(COMANDO);

        // Assert
        assertEquals("Camilla grande", actualizado.getNombre());
        assertEquals(new BigDecimal("70000.00"), actualizado.getPrecioUnitario());
        assertEquals(5, actualizado.getStock());
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(COMANDO));
        verify(recursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("No permite actualizar un recurso desactivado")
    void rejectsDeactivatedResource() {
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(resource(LocalDateTime.of(2026, 9, 2, 8, 0))));

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> service.ejecutar(COMANDO));

        assertEquals("No se puede actualizar un recurso desactivado", ex.getMessage());
        verify(recursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza un nombre que ya usa otro recurso activo del proveedor")
    void rejectsDuplicatedName() {
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(resource(null)));
        when(recursoRepositoryPort.existeNombreActivo(7, "Camilla grande", 1)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> service.ejecutar(COMANDO));
        verify(recursoRepositoryPort, never()).guardar(any());
    }
}
