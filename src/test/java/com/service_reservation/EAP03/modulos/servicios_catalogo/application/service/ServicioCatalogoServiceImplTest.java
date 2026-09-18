package com.service_reservation.EAP03.modulos.servicios_catalogo.application.service;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCatalogoServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    private ServicioCatalogoServiceImpl servicioCatalogoService;

    @BeforeEach
    void setUp() {
        servicioCatalogoService = new ServicioCatalogoServiceImpl(servicioRepository);
    }

    @Test
    @DisplayName("Debe ejecutar el caso de uso y guardar el servicio correctamente")
    void debeEjecutarCasoDeUsoCrearServicio() {
        CrearServicioRequest request = new CrearServicioRequest(10, "Mantenimiento Preventivo", 90);

        OffsetDateTime fechaSimulada = OffsetDateTime.now();
        Servicio servicioGuardadoMock = Servicio.reconstituir(100, 10, "Mantenimiento Preventivo", 90, fechaSimulada);

        when(servicioRepository.guardar(any(Servicio.class))).thenReturn(servicioGuardadoMock);

        ServicioResponse response = servicioCatalogoService.ejecutar(request);

        assertNotNull(response);
        assertEquals(100, response.id());
        assertEquals(10, response.idProveedor());
        assertEquals("Mantenimiento Preventivo", response.nombre());
        assertEquals(90, response.duracionMinutos());
        assertEquals(fechaSimulada, response.createdAt());

        ArgumentCaptor<Servicio> captor = ArgumentCaptor.forClass(Servicio.class);
        verify(servicioRepository, times(1)).guardar(captor.capture());

        Servicio servicioEnviado = captor.getValue();
        assertEquals(10, servicioEnviado.getIdProveedor());
        assertEquals("Mantenimiento Preventivo", servicioEnviado.getNombre());
        assertEquals(90, servicioEnviado.getDuracionMinutos());
        assertNull(servicioEnviado.getId());
    }
}
