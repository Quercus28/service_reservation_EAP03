package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDuplicadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.CrearRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearRecursoServiceTest {

    private static final CrearRecursoComando COMANDO = new CrearRecursoComando(7, "Camilla", new BigDecimal("50000.00"), 3);

    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private CrearRecursoService service;

    @Test
    @DisplayName("Crea y guarda un recurso nuevo activo")
    void createsResource() {
        // Arrange
        when(recursoRepositoryPort.existeNombreActivo(7, "Camilla", null)).thenReturn(false);
        when(recursoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Recurso recurso = service.ejecutar(COMANDO);

        // Assert
        assertNull(recurso.getId());
        assertEquals(7, recurso.getIdProveedor());
        assertEquals("Camilla", recurso.getNombre());
        assertEquals(3, recurso.getStock());
        assertTrue(recurso.estaActivo());
    }

    @Test
    @DisplayName("Rechaza un nombre que ya usa otro recurso activo del proveedor")
    void rejectsDuplicatedName() {
        when(recursoRepositoryPort.existeNombreActivo(7, "Camilla", null)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> service.ejecutar(COMANDO));
        verify(recursoRepositoryPort, never()).guardar(any());
    }
}
