package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarRecursosServiceTest {

    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private ConsultarRecursosService service;

    @Test
    @DisplayName("Devuelve la página de recursos activos del proveedor")
    void returnsActiveResourcesPage() {
        ResultadoPaginado<Recurso> pagina = new ResultadoPaginado<>(List.of(), 0, 20, 0L);
        when(recursoRepositoryPort.buscarActivosPorProveedor(7, 0, 20)).thenReturn(pagina);

        assertSame(pagina, service.ejecutar(7, 0, 20));
    }
}
