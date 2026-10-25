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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerRecursoPorIdServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private ObtenerRecursoPorIdService service;

    @Test
    @DisplayName("Devuelve el recurso encontrado")
    void returnsResource() {
        Recurso recurso = Recurso.nuevo(7, "Camilla", BigDecimal.TEN, 3);
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recurso));

        assertSame(recurso, service.ejecutar(1));
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(1));
    }
}
