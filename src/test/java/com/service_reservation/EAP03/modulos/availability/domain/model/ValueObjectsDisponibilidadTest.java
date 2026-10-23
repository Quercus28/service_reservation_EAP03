package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsDisponibilidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);

    // ---- DateRange ----

    @Test
    @DisplayName("DateRange de un solo día cuenta 1 día")
    void dateRangeMismoDia() {
        // Arrange
        DateRange rango = new DateRange(HOY, HOY);

        // Act
        long dias = rango.totalDias();

        // Assert
        assertEquals(1, dias);
    }

    @Test
    @DisplayName("DateRange cuenta ambos extremos del rango")
    void dateRangeIncluyeExtremos() {
        // Arrange
        DateRange rango = new DateRange(HOY, HOY.plusDays(29));

        // Act
        long dias = rango.totalDias();

        // Assert
        assertEquals(30, dias);
    }

    @Test
    @DisplayName("DateRange rechaza startDate nulo")
    void dateRangeRechazaInicioNulo() {
        // Arrange
        LocalDate fin = HOY;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new DateRange(null, fin));

        // Assert
        assertEquals("startDate es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("DateRange rechaza endDate nulo")
    void dateRangeRechazaFinNulo() {
        // Arrange
        LocalDate inicio = HOY;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new DateRange(inicio, null));

        // Assert
        assertEquals("endDate es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("DateRange rechaza endDate anterior a startDate")
    void dateRangeRechazaFinAnterior() {
        // Arrange
        LocalDate ayer = HOY.minusDays(1);

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> new DateRange(HOY, ayer));

        // Assert
        assertEquals("endDate no puede ser anterior a startDate", ex.getMessage());
    }

    // ---- HorarioSemanal ----

    @Test
    @DisplayName("HorarioSemanal válido conserva sus datos")
    void horarioSemanalValido() {
        // Arrange
        DiaSemana dia = DiaSemana.LUNES;

        // Act
        HorarioSemanal horario = new HorarioSemanal(dia, OCHO, DOCE);

        // Assert
        assertEquals(DiaSemana.LUNES, horario.diaSemana());
        assertEquals(OCHO, horario.horaInicio());
        assertEquals(DOCE, horario.horaFin());
    }

    @Test
    @DisplayName("HorarioSemanal rechaza día nulo")
    void horarioSemanalRechazaDiaNulo() {
        // Arrange
        DiaSemana dia = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new HorarioSemanal(dia, OCHO, DOCE));

        // Assert
        assertEquals("diaSemana es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("HorarioSemanal rechaza hora de inicio nula")
    void horarioSemanalRechazaInicioNulo() {
        // Arrange
        LocalTime inicio = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new HorarioSemanal(DiaSemana.LUNES, inicio, DOCE));

        // Assert
        assertEquals("horaInicio es obligatoria", ex.getMessage());
    }

    @Test
    @DisplayName("HorarioSemanal rechaza hora de fin nula")
    void horarioSemanalRechazaFinNulo() {
        // Arrange
        LocalTime fin = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new HorarioSemanal(DiaSemana.LUNES, OCHO, fin));

        // Assert
        assertEquals("horaFin es obligatoria", ex.getMessage());
    }

    @ParameterizedTest(name = "horaFin = {0}")
    @ValueSource(strings = {"08:00", "07:00"})
    @DisplayName("HorarioSemanal rechaza horaFin igual o anterior a horaInicio")
    void horarioSemanalRechazaFinNoPosterior(String horaFin) {
        // Arrange
        LocalTime fin = LocalTime.parse(horaFin);

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> new HorarioSemanal(DiaSemana.LUNES, OCHO, fin));

        // Assert
        assertEquals("horaFin debe ser posterior a horaInicio", ex.getMessage());
    }

    // ---- ServicioDisponibilidadInfo ----

    @Test
    @DisplayName("ServicioDisponibilidadInfo válido conserva sus datos")
    void servicioInfoValido() {
        // Arrange
        Integer idServicio = 10;
        Integer idProveedor = 7;

        // Act
        ServicioDisponibilidadInfo info = new ServicioDisponibilidadInfo(idServicio, idProveedor, 45);

        // Assert
        assertEquals(10, info.id());
        assertEquals(7, info.idProveedor());
        assertEquals(45, info.duracionMinutos());
    }

    @Test
    @DisplayName("ServicioDisponibilidadInfo rechaza id de servicio nulo")
    void servicioInfoRechazaIdNulo() {
        // Arrange
        Integer idServicio = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new ServicioDisponibilidadInfo(idServicio, 7, 45));

        // Assert
        assertEquals("El id del servicio es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("ServicioDisponibilidadInfo rechaza id de proveedor nulo")
    void servicioInfoRechazaProveedorNulo() {
        // Arrange
        Integer idProveedor = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new ServicioDisponibilidadInfo(10, idProveedor, 45));

        // Assert
        assertEquals("El id del proveedor es obligatorio", ex.getMessage());
    }

    @ParameterizedTest(name = "duración = {0}")
    @ValueSource(ints = {0, -15})
    @DisplayName("ServicioDisponibilidadInfo rechaza duración cero o negativa")
    void servicioInfoRechazaDuracionInvalida(int duracion) {
        // Arrange
        Integer idServicio = 10;

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ServicioDisponibilidadInfo(idServicio, 7, duracion));

        // Assert
        assertEquals("La duración del servicio debe ser mayor a 0 minutos", ex.getMessage());
    }

    // ---- Excepciones ----

    @Test
    @DisplayName("ServicioParaDisponibilidadNoEncontradoException incluye el id en el mensaje")
    void mensajeServicioNoEncontrado() {
        // Arrange
        Integer idServicio = 99;

        // Act
        ServicioParaDisponibilidadNoEncontradoException ex =
                new ServicioParaDisponibilidadNoEncontradoException(idServicio);

        // Assert
        assertEquals("Servicio con ID 99 no fue encontrado o no está disponible", ex.getMessage());
    }
}
