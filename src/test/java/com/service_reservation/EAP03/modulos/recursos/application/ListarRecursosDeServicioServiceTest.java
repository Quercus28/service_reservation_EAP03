package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.ServicioNoExisteException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarRecursosDeServicioServiceTest {

    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    @Mock private ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    @InjectMocks private ListarRecursosDeServicioService service;

    @Test
    @DisplayName("Devuelve los recursos asociados al servicio")
    void returnsLinkedResources() {
        List<ServicioRecurso> asociados = List.of(ServicioRecurso.reconstruir(1, 10, 3, 1));
        when(obtenerServicioPorIdUseCase.ejecutar(10)).thenReturn(new ServicioResponse(10, 7, "Masaje", 60, null));
        when(servicioRecursoRepositoryPort.buscarPorServicio(10)).thenReturn(asociados);

        assertEquals(asociados, service.ejecutar(10));
    }

    @Test
    @DisplayName("Falla si el servicio no existe")
    void unknownService() {
        when(obtenerServicioPorIdUseCase.ejecutar(10)).thenThrow(new ServicioNoEncontradoException(10));

        assertThrows(ServicioNoExisteException.class, () -> service.ejecutar(10));
        verifyNoInteractions(servicioRecursoRepositoryPort);
    }
}
