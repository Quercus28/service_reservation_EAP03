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
        // Arrange
        Integer idServicio = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(idServicio, HOY, MANANA));

        // Assert
        assertEquals("El parámetro 'id' del servicio es obligatorio", ex.getMessage());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Rechaza startDate ausente")
    void rechazaInicioAusente() {
        // Arrange
        LocalDate inicio = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, inicio, MANANA));

        // Assert
        assertEquals("El parámetro 'startDate' es obligatorio y debe tener formato ISO YYYY-MM-DD", ex.getMessage());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Rechaza endDate ausente")
    void rechazaFinAusente() {
        // Arrange
        LocalDate fin = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, HOY, fin));

        // Assert
        assertEquals("El parámetro 'endDate' es obligatorio y debe tener formato ISO YYYY-MM-DD", ex.getMessage());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Rechaza endDate anterior a startDate")
    void rechazaFinAnterior() {
        // Arrange
        LocalDate inicio = MANANA;
        LocalDate fin = HOY;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> controller.consultarDisponibilidad(ID_SERVICIO, inicio, fin));

        // Assert
        assertEquals("El parámetro 'endDate' no puede ser anterior a 'startDate'", ex.getMessage());
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Sin espacios libres responde 200 con mensaje y sin bloques")
    void sinDisponibilidad() {
        // Arrange
        when(useCase.ejecutar(ID_SERVICIO, HOY, MANANA)).thenReturn(List.of());

        // Act
        ResponseEntity<DisponibilidadServicioResponseDTO> respuesta =
                controller.consultarDisponibilidad(ID_SERVICIO, HOY, MANANA);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(new DisponibilidadServicioResponseDTO(
                ID_SERVICIO, 0, List.of(), DisponibilidadServicioResponseDTO.MENSAJE_SIN_ESPACIOS), respuesta.getBody());
    }

    @Test
    @DisplayName("Agrupa las franjas por fecha y las ordena cronológicamente")
    void agrupaYOrdenaPorFecha() {
        // Arrange
        when(useCase.ejecutar(ID_SERVICIO, HOY, MANANA)).thenReturn(List.of(
                franja(MANANA, 9),
                franja(HOY, 15),
                franja(MANANA, 8),
                franja(HOY, 11)));
        List<BloqueDisponibilidadDTO> bloquesEsperados = List.of(
                new BloqueDisponibilidadDTO(HOY, List.of(franjaDto(11), franjaDto(15))),
                new BloqueDisponibilidadDTO(MANANA, List.of(franjaDto(8), franjaDto(9))));

        // Act
        ResponseEntity<DisponibilidadServicioResponseDTO> respuesta =
                controller.consultarDisponibilidad(ID_SERVICIO, HOY, MANANA);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(new DisponibilidadServicioResponseDTO(ID_SERVICIO, 4, bloquesEsperados, null), respuesta.getBody());
    }

    @Test
    @DisplayName("Acepta consultar un único día")
    void consultaUnSoloDia() {
        // Arrange
        when(useCase.ejecutar(ID_SERVICIO, HOY, HOY)).thenReturn(List.of(franja(HOY, 11)));

        // Act
        ResponseEntity<DisponibilidadServicioResponseDTO> respuesta =
                controller.consultarDisponibilidad(ID_SERVICIO, HOY, HOY);

        // Assert
        assertEquals(new DisponibilidadServicioResponseDTO(ID_SERVICIO, 1,
                List.of(new BloqueDisponibilidadDTO(HOY, List.of(franjaDto(11)))), null), respuesta.getBody());
        verify(useCase).ejecutar(ID_SERVICIO, HOY, HOY);
    }
}
