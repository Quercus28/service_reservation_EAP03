package com.service_reservation.EAP03.modulos.availability.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
import com.service_reservation.EAP03.modulos.availability.domain.service.DisponibilidadCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadServicioServiceTest {

    private static final Integer ID_SERVICIO = 10;
    private static final Integer ID_PROVEEDOR = 7;
    private static final int DURACION = 60;
    // "Ahora" fijo: lunes 2026-11-02 a las 10:00
    private static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-02T10:00:00Z"), ZoneOffset.UTC);

    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private DisponibilidadCalculator disponibilidadCalculator;

    private ConsultarDisponibilidadServicioService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarDisponibilidadServicioService(availabilityRepository, disponibilidadCalculator, CLOCK);
    }

    private ServicioDisponibilidadInfo servicioInfo() {
        return new ServicioDisponibilidadInfo(ID_SERVICIO, ID_PROVEEDOR, DURACION);
    }

    private TimeSlot franja(LocalDate fecha, int hora) {
        return new TimeSlot(fecha.atTime(hora, 0), fecha.atTime(hora + 1, 0));
    }

    @Test
    @DisplayName("Rechaza id de servicio nulo")
    void rechazaIdNulo() {
        // Arrange
        Integer idServicio = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> service.ejecutar(idServicio, HOY, HOY));

        // Assert
        assertEquals("El id del servicio es obligatorio", ex.getMessage());
        verifyNoInteractions(availabilityRepository, disponibilidadCalculator);
    }

    @Test
    @DisplayName("Rechaza startDate nulo")
    void rechazaInicioNulo() {
        // Arrange
        LocalDate inicio = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> service.ejecutar(ID_SERVICIO, inicio, HOY));

        // Assert
        assertEquals("Las fechas de inicio y fin son obligatorias", ex.getMessage());
        verifyNoInteractions(availabilityRepository, disponibilidadCalculator);
    }

    @Test
    @DisplayName("Rechaza endDate nulo")
    void rechazaFinNulo() {
        // Arrange
        LocalDate fin = null;

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> service.ejecutar(ID_SERVICIO, HOY, fin));

        // Assert
        assertEquals("Las fechas de inicio y fin son obligatorias", ex.getMessage());
        verifyNoInteractions(availabilityRepository, disponibilidadCalculator);
    }

    @Test
    @DisplayName("Rechaza una fecha de inicio en el pasado")
    void rechazaInicioPasado() {
        // Arrange
        LocalDate ayer = HOY.minusDays(1);

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> service.ejecutar(ID_SERVICIO, ayer, HOY));

        // Assert
        assertEquals("La fecha de inicio (startDate) no puede ser una fecha pasada", ex.getMessage());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    @DisplayName("Rechaza endDate anterior a startDate")
    void rechazaFinAnteriorAInicio() {
        // Arrange
        LocalDate inicio = HOY.plusDays(2);
        LocalDate fin = HOY.plusDays(1);

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> service.ejecutar(ID_SERVICIO, inicio, fin));

        // Assert
        assertEquals("endDate no puede ser anterior a startDate", ex.getMessage());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    @DisplayName("Rechaza rangos de más de 30 días")
    void rechazaRangoExcedido() {
        // Arrange
        LocalDate fin = HOY.plusDays(30); // 31 días contando ambos extremos

        // Act
        RangoFechasExcedidoException ex = assertThrows(RangoFechasExcedidoException.class,
                () -> service.ejecutar(ID_SERVICIO, HOY, fin));

        // Assert
        assertEquals("El rango de consulta no puede superar los 30 días", ex.getMessage());
        verifyNoInteractions(availabilityRepository);
    }

    @Test
    @DisplayName("Acepta un rango de exactamente 30 días")
    void aceptaRangoDeTreintaDias() {
        // Arrange
        LocalDate fin = HOY.plusDays(29);
        when(availabilityRepository.obtenerServicioInfo(ID_SERVICIO)).thenReturn(Optional.of(servicioInfo()));
        when(disponibilidadCalculator.calcularDisponibilidad(any(), any(), any(), anyInt())).thenReturn(List.of());

        // Act
        List<TimeSlot> resultado = service.ejecutar(ID_SERVICIO, HOY, fin);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(disponibilidadCalculator).calcularDisponibilidad(eq(new DateRange(HOY, fin)), any(), any(), eq(DURACION));
    }

    @Test
    @DisplayName("Lanza excepción si el servicio no existe")
    void servicioNoEncontrado() {
        // Arrange
        when(availabilityRepository.obtenerServicioInfo(ID_SERVICIO)).thenReturn(Optional.empty());

        // Act
        ServicioParaDisponibilidadNoEncontradoException ex = assertThrows(
                ServicioParaDisponibilidadNoEncontradoException.class,
                () -> service.ejecutar(ID_SERVICIO, HOY, HOY));

        // Assert
        assertEquals("Servicio con ID 10 no fue encontrado o no está disponible", ex.getMessage());
        verify(availabilityRepository, never()).obtenerHorariosProveedor(any(), any());
        verifyNoInteractions(disponibilidadCalculator);
    }

    @Test
    @DisplayName("Consulta horarios y reservas del proveedor y delega el cálculo")
    void delegaCalculoConDatosDelRepositorio() {
        // Arrange
        LocalDate fin = HOY.plusDays(3);
        List<HorarioSemanal> horarios = List.of(
                new HorarioSemanal(DiaSemana.MARTES, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        List<TimeSlot> ocupadas = List.of(franja(fin, 8));
        List<TimeSlot> calculadas = List.of(franja(HOY.plusDays(1), 8));
        when(availabilityRepository.obtenerServicioInfo(ID_SERVICIO)).thenReturn(Optional.of(servicioInfo()));
        when(availabilityRepository.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR)).thenReturn(horarios);
        when(availabilityRepository.obtenerReservasActivas(
                ID_SERVICIO, ID_PROVEEDOR, HOY.atStartOfDay(), fin.atTime(LocalTime.MAX))).thenReturn(ocupadas);
        when(disponibilidadCalculator.calcularDisponibilidad(new DateRange(HOY, fin), horarios, ocupadas, DURACION))
                .thenReturn(calculadas);

        // Act
        List<TimeSlot> resultado = service.ejecutar(ID_SERVICIO, HOY, fin);

        // Assert
        assertEquals(calculadas, resultado);
    }

    @Test
    @DisplayName("Descarta las franjas de hoy que ya pasaron o empiezan justo ahora")
    void descartaFranjasPasadas() {
        // Arrange
        TimeSlot pasada = franja(HOY, 8);
        TimeSlot ahoraMismo = franja(HOY, 10);
        TimeSlot futuraHoy = franja(HOY, 11);
        TimeSlot manana = franja(HOY.plusDays(1), 8);
        when(availabilityRepository.obtenerServicioInfo(ID_SERVICIO)).thenReturn(Optional.of(servicioInfo()));
        when(availabilityRepository.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR)).thenReturn(List.of());
        when(availabilityRepository.obtenerReservasActivas(eq(ID_SERVICIO), eq(ID_PROVEEDOR), any(), any()))
                .thenReturn(List.of());
        when(disponibilidadCalculator.calcularDisponibilidad(any(), any(), any(), anyInt()))
                .thenReturn(List.of(pasada, ahoraMismo, futuraHoy, manana));

        // Act
        List<TimeSlot> resultado = service.ejecutar(ID_SERVICIO, HOY, HOY.plusDays(1));

        // Assert
        assertEquals(List.of(futuraHoy, manana), resultado);
    }

    @Test
    @DisplayName("Integra con el calculador real y respeta reservas y hora actual")
    void integraConCalculadorReal() {
        // Arrange
        ConsultarDisponibilidadServicioService serviceReal = new ConsultarDisponibilidadServicioService(
                availabilityRepository, new DisponibilidadCalculator(), CLOCK);
        List<HorarioSemanal> horarios = List.of(
                new HorarioSemanal(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(14, 0)));
        when(availabilityRepository.obtenerServicioInfo(ID_SERVICIO)).thenReturn(Optional.of(servicioInfo()));
        when(availabilityRepository.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR)).thenReturn(horarios);
        when(availabilityRepository.obtenerReservasActivas(eq(ID_SERVICIO), eq(ID_PROVEEDOR), any(), any()))
                .thenReturn(List.of(franja(HOY, 12)));

        // Act
        List<TimeSlot> resultado = serviceReal.ejecutar(ID_SERVICIO, HOY, HOY);

        // Assert: 08-10 ya pasaron, 10:00 empieza ahora, 12-13 está reservado
        assertEquals(List.of(franja(HOY, 11), franja(HOY, 13)), resultado);
    }
}
