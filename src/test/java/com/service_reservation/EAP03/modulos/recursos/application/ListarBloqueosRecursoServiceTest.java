package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarBloqueosRecursoServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;

    @InjectMocks private ListarBloqueosRecursoService service;

    @Test
    @DisplayName("Devuelve la página de bloqueos del recurso")
    void returnsBlocksPage() {
        ResultadoPaginado<BloqueoRecurso> pagina = new ResultadoPaginado<>(List.of(), 0, 20, 0L);
        when(recursoRepositoryPort.existePorId(1)).thenReturn(true);
        when(bloqueoRecursoRepositoryPort.buscarPorRecurso(1, 0, 20)).thenReturn(pagina);

        assertSame(pagina, service.ejecutar(1, 0, 20));
    }

    @Test
    @DisplayName("Rechaza una página negativa o un tamaño menor que uno")
    void rejectsInvalidPaging() {
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(1, -1, 20));
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(1, 0, 0));
        verifyNoInteractions(recursoRepositoryPort, bloqueoRecursoRepositoryPort);
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.existePorId(1)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(1, 0, 20));
        verifyNoInteractions(bloqueoRecursoRepositoryPort);
    }
}
