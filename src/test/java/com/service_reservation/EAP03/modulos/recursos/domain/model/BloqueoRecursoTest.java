package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoRecursoTest {

    private static final LocalDateTime INICIO = LocalDateTime.now().plusDays(1).withNano(0);
    private static final LocalDateTime FIN = INICIO.plusHours(2);

    @Test
    @DisplayName("Crea un bloqueo nuevo sin id, con motivo recortado y fecha de creación")
    void createsNewBlock() {
        // Act
        BloqueoRecurso bloqueo = BloqueoRecurso.nuevo(1, INICIO, FIN, "  Mantenimiento  ");

        // Assert
        assertNull(bloqueo.getId());
        assertEquals(1, bloqueo.getIdRecurso());
        assertEquals(INICIO, bloqueo.getFechaInicio());
        assertEquals(FIN, bloqueo.getFechaFin());
        assertEquals("Mantenimiento", bloqueo.getMotivo());
        assertNotNull(bloqueo.getCreatedAt());
    }

    @ParameterizedTest(name = "motivo = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Un motivo nulo o en blanco se guarda como nulo")
    void blankReasonBecomesNull(String motivo) {
        assertNull(BloqueoRecurso.nuevo(1, INICIO, FIN, motivo).getMotivo());
    }

    @Test
    @DisplayName("Acepta un motivo de exactamente 255 caracteres")
    void acceptsReasonAtLimit() {
        assertEquals(255, BloqueoRecurso.nuevo(1, INICIO, FIN, "a".repeat(255)).getMotivo().length());
    }

    @Test
    @DisplayName("Rechaza un motivo de más de 255 caracteres")
    void rejectsTooLongReason() {
        String motivo = "a".repeat(256);

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> BloqueoRecurso.nuevo(1, INICIO, FIN, motivo));

        assertEquals("El motivo no puede superar los 255 caracteres", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza un bloqueo nuevo que ya terminó")
    void rejectsNewBlockInThePast() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        LocalDateTime fin = inicio.plusHours(1);

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> BloqueoRecurso.nuevo(1, inicio, fin, null));

        assertEquals("fechaFin no puede estar en el pasado", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza fechas nulas")
    void rejectsNullDates() {
        assertThrows(DatosInvalidosException.class, () -> BloqueoRecurso.nuevo(1, null, FIN, null));
        assertThrows(DatosInvalidosException.class, () -> BloqueoRecurso.nuevo(1, INICIO, null, null));
    }

    @Test
    @DisplayName("Rechaza una fecha de fin que no sea posterior al inicio")
    void rejectsEndNotAfterStart() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> BloqueoRecurso.nuevo(1, FIN, INICIO, null));

        assertEquals("fechaFin debe ser posterior a fechaInicio", ex.getMessage());
        assertThrows(DatosInvalidosException.class, () -> BloqueoRecurso.nuevo(1, INICIO, INICIO, null));
    }

    @Test
    @DisplayName("Rechaza un bloqueo sin recurso")
    void rejectsNullResource() {
        assertThrows(NullPointerException.class, () -> BloqueoRecurso.nuevo(null, INICIO, FIN, null));
    }

    @Test
    @DisplayName("Reconstruye un bloqueo ya vencido sin rechazarlo")
    void rebuildsExpiredBlock() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2020, 1, 1, 8, 0);
        LocalDateTime creado = LocalDateTime.of(2019, 12, 31, 8, 0);

        // Act
        BloqueoRecurso bloqueo = BloqueoRecurso.reconstruir(7, 1, inicio, inicio.plusHours(1), "Viejo", creado);

        // Assert
        assertEquals(7, bloqueo.getId());
        assertEquals(creado, bloqueo.getCreatedAt());
    }

    @Test
    @DisplayName("Se solapa cuando los rangos se cruzan y no cuando solo se tocan en el borde")
    void overlaps() {
        BloqueoRecurso bloqueo = BloqueoRecurso.nuevo(1, INICIO, FIN, null);

        assertTrue(bloqueo.seSolapaCon(INICIO.plusHours(1), FIN.plusHours(1)));
        assertTrue(bloqueo.seSolapaCon(INICIO.minusHours(1), INICIO.plusMinutes(1)));
        assertFalse(bloqueo.seSolapaCon(FIN, FIN.plusHours(1)));
        assertFalse(bloqueo.seSolapaCon(INICIO.minusHours(1), INICIO));
    }
}
