package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.controller;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.in.ConsultarDisponibilidadServicioUseCase;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.BloqueDisponibilidadDTO;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.BloqueDisponibilidadDTO.FranjaHorariaDTO;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.DisponibilidadServicioResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityControllerTest {

    private static final Integer ID_SERVICIO = 10;
    private static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private static final LocalDate MANANA = HOY.plusDays(1);

    @Mock private ConsultarDisponibilidadServicioUseCase useCase;

    @InjectMocks private AvailabilityController controller;

    private TimeSlot franja(LocalDate fecha, int hora) {
        return new TimeSlot(fecha.atTime(hora, 0), fecha.atTime(hora + 1, 0));
    }

    private FranjaHorariaDTO franjaDto(int hora) {
        return new FranjaHorariaDTO(LocalTime.of(hora, 0), LocalTime.of(hora + 1, 0));
    }

    @Test
    @DisplayName("Rechaza id nulo")
    void rechazaIdNulo() {
        assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(null, HOY, MANANA));
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Rechaza startDate o endDate ausentes")
    void rechazaFechasAusentes() {
        DisponibilidadInvalidaException sinInicio = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, null, MANANA));
        DisponibilidadInvalidaException sinFin = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, HOY, null));

        assertTrue(sinInicio.getMessage().contains("startDate"));
        assertTrue(sinFin.getMessage().contains("endDate"));
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Rechaza endDate anterior a startDate")
    void rechazaFinAnterior() {
        assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, MANANA, HOY));
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Sin espacios libres responde 200 con mensaje y sin bloques")
    void sinDisponibilidad() {
        when(useCase.ejecutar(ID_SERVICIO, HOY, MANANA)).thenReturn(List.of());

        ResponseEntity<DisponibilidadServicioResponseDTO> respuesta =
                controller.consultarDisponibilidad(ID_SERVICIO, HOY, MANANA);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        DisponibilidadServicioResponseDTO body = respuesta.getBody();
        assertNotNull(body);
        assertEquals(ID_SERVICIO, body.servicioId());
        assertEquals(0, body.totalEspaciosDisponibles());
        assertTrue(body.disponibilidad().isEmpty());
        assertEquals(DisponibilidadServicioResponseDTO.MENSAJE_SIN_ESPACIOS, body.mensaje());
    }

    @Test
    @DisplayName("Agrupa las franjas por fecha y las ordena cronológicamente")
    void agrupaYOrdenaPorFecha() {
        when(useCase.ejecutar(ID_SERVICIO, HOY, MANANA)).thenReturn(List.of(
                franja(MANANA, 9),
                franja(HOY, 15),
                franja(MANANA, 8),
                franja(HOY, 11)));

        ResponseEntity<DisponibilidadServicioResponseDTO> respuesta =
                controller.consultarDisponibilidad(ID_SERVICIO, HOY, MANANA);

        DisponibilidadServicioResponseDTO body = respuesta.getBody();
        assertNotNull(body);
        assertEquals(4, body.totalEspaciosDisponibles());
        assertNull(body.mensaje());
        assertEquals(List.of(
                new BloqueDisponibilidadDTO(HOY, List.of(franjaDto(11), franjaDto(15))),
                new BloqueDisponibilidadDTO(MANANA, List.of(franjaDto(8), franjaDto(9)))), body.disponibilidad());
    }

    @Test
    @DisplayName("Acepta consultar un único día")
    void consultaUnSoloDia() {
        when(useCase.ejecutar(ID_SERVICIO, HOY, HOY)).thenReturn(List.of(franja(HOY, 11)));

        DisponibilidadServicioResponseDTO body = controller.consultarDisponibilidad(ID_SERVICIO, HOY, HOY).getBody();

        assertNotNull(body);
        assertEquals(1, body.disponibilidad().size());
        verify(useCase).ejecutar(ID_SERVICIO, HOY, HOY);
    }
}
