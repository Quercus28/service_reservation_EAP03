package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    private static final LocalDateTime OCHO = LocalDateTime.of(2026, 11, 2, 8, 0);

    private TimeSlot slot(int horaInicio, int minInicio, int horaFin, int minFin) {
        return new TimeSlot(OCHO.withHour(horaInicio).withMinute(minInicio), OCHO.withHour(horaFin).withMinute(minFin));
    }

    @Test
    @DisplayName("Crea la franja cuando el fin es posterior al inicio")
    void creaFranjaValida() {
        // Arrange
        LocalDateTime fin = OCHO.plusHours(1);

        // Act
        TimeSlot slot = new TimeSlot(OCHO, fin);

        // Assert
        assertEquals(OCHO, slot.start());
        assertEquals(fin, slot.end());
    }

    @Test
    @DisplayName("Rechaza un inicio nulo")
    void rechazaInicioNulo() {
        // Arrange
        LocalDateTime fin = OCHO.plusHours(1);

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new TimeSlot(null, fin));

        // Assert
        assertEquals("La fecha/hora de inicio es obligatoria", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza un fin nulo")
    void rechazaFinNulo() {
        // Arrange
        LocalDateTime inicio = OCHO;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new TimeSlot(inicio, null));

        // Assert
        assertEquals("La fecha/hora de fin es obligatoria", ex.getMessage());
    }

    @ParameterizedTest(name = "fin desplazado {0} minutos")
    @CsvSource({"0", "-1", "-60"})
    @DisplayName("Rechaza una franja con fin igual o anterior al inicio")
    void rechazaFinNoPosterior(int minutosDesplazamiento) {
        // Arrange
        LocalDateTime fin = OCHO.plusMinutes(minutosDesplazamiento);

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> new TimeSlot(OCHO, fin));

        // Assert
        assertEquals("La fecha/hora final debe ser estrictamente posterior a la inicial", ex.getMessage());
    }

    @ParameterizedTest(name = "[{0}:{1} - {2}:{3}] se solapa con 08:00-09:00 -> {4}")
    @CsvSource({
            "8, 30, 9, 30, true",   // solapa el final
            "7, 30, 8, 30, true",   // solapa el inicio
            "8, 15, 8, 45, true",   // queda contenida
            "7, 0, 10, 0, true",    // la envuelve
            "9, 0, 10, 0, false",   // contigua por detrás
            "7, 0, 8, 0, false",    // contigua por delante
            "11, 0, 12, 0, false"   // separada
    })
    @DisplayName("Detecta correctamente el solapamiento con otra franja")
    void detectaSolapamiento(int hIni, int mIni, int hFin, int mFin, boolean esperado) {
        // Arrange
        TimeSlot base = slot(8, 0, 9, 0);
        TimeSlot otra = slot(hIni, mIni, hFin, mFin);

        // Act
        boolean resultado = base.seSolapaCon(otra);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("No se solapa con una franja nula")
    void noSolapaConNull() {
        // Arrange
        TimeSlot base = slot(8, 0, 9, 0);

        // Act
        boolean resultado = base.seSolapaCon(null);

        // Assert
        assertFalse(resultado);
    }

    @ParameterizedTest(name = "08:00-10:00 contiene [{0}:{1} - {2}:{3}] -> {4}")
    @CsvSource({
            "8, 0, 10, 0, true",    // idéntica
            "8, 30, 9, 30, true",   // interna
            "7, 30, 9, 0, false",   // sobresale al inicio
            "9, 0, 10, 30, false"   // sobresale al final
    })
    @DisplayName("Contiene franjas internas o iguales, pero no las que sobresalen")
    void contiene(int hIni, int mIni, int hFin, int mFin, boolean esperado) {
        // Arrange
        TimeSlot base = slot(8, 0, 10, 0);
        TimeSlot otra = slot(hIni, mIni, hFin, mFin);

        // Act
        boolean resultado = base.contiene(otra);

        // Assert
        assertEquals(esperado, resultado);
    }

    @Test
    @DisplayName("No contiene una franja nula")
    void noContieneNull() {
        // Arrange
        TimeSlot base = slot(8, 0, 10, 0);

        // Act
        boolean resultado = base.contiene(null);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Una franja que empieza antes se ordena primero")
    void comparaPorInicioMenor() {
        // Arrange
        TimeSlot temprano = slot(8, 0, 9, 0);
        TimeSlot tarde = slot(9, 0, 10, 0);

        // Act
        int resultado = temprano.compareTo(tarde);

        // Assert
        assertTrue(resultado < 0);
    }

    @Test
    @DisplayName("Una franja que empieza después se ordena al final")
    void comparaPorInicioMayor() {
        // Arrange
        TimeSlot temprano = slot(8, 0, 9, 0);
        TimeSlot tarde = slot(9, 0, 10, 0);

        // Act
        int resultado = tarde.compareTo(temprano);

        // Assert
        assertTrue(resultado > 0);
    }

    @Test
    @DisplayName("Ante el mismo inicio, ordena por fin")
    void comparaPorFinCuandoEmpatanInicio() {
        // Arrange
        TimeSlot corta = slot(8, 0, 9, 0);
        TimeSlot larga = slot(8, 0, 10, 0);

        // Act
        int resultado = corta.compareTo(larga);

        // Assert
        assertTrue(resultado < 0);
    }

    @Test
    @DisplayName("Dos franjas iguales comparan como iguales")
    void comparaIguales() {
        // Arrange
        TimeSlot una = slot(8, 0, 9, 0);
        TimeSlot otra = slot(8, 0, 9, 0);

        // Act
        int resultado = una.compareTo(otra);

        // Assert
        assertEquals(0, resultado);
    }
}
