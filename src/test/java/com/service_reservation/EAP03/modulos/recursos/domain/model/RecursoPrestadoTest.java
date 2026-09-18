package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RecursoPrestadoTest {

    private static final BigDecimal PRECIO = new BigDecimal("100000.00");
    private static final LocalDateTime DIEZ = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime DOCE = LocalDateTime.of(2026, 10, 1, 12, 0);

    private RecursoPrestado prestamoDeDiezADoce() {
        return RecursoPrestado.nuevo(1, 1, 2, DIEZ, DOCE, PRECIO);
    }

    @Test
    @DisplayName("Crea un prestamo valido")
    void creaPrestamoValido() {
        RecursoPrestado prestamo = prestamoDeDiezADoce();

        assertNull(prestamo.getId());
        assertEquals(2, prestamo.getCantidad());
        assertEquals(DIEZ, prestamo.getFechaInicio());
    }

    @Test
    @DisplayName("Rechaza que la fecha de fin no sea posterior al inicio")
    void rechazaFechasInvertidas() {
        assertThrows(DatosInvalidosException.class,
                () -> RecursoPrestado.nuevo(1, 1, 2, DOCE, DIEZ, PRECIO));
        assertThrows(DatosInvalidosException.class,
                () -> RecursoPrestado.nuevo(1, 1, 2, DIEZ, DIEZ, PRECIO));
    }

    @Test
    @DisplayName("Rechaza cantidad menor o igual a cero")
    void rechazaCantidadInvalida() {
        assertThrows(DatosInvalidosException.class,
                () -> RecursoPrestado.nuevo(1, 1, 0, DIEZ, DOCE, PRECIO));
    }

    @Test
    @DisplayName("Rechaza precio total negativo")
    void rechazaPrecioNegativo() {
        assertThrows(DatosInvalidosException.class,
                () -> RecursoPrestado.nuevo(1, 1, 2, DIEZ, DOCE, new BigDecimal("-1")));
    }

    @Test
    @DisplayName("Se solapa cuando los rangos se cruzan a medias")
    void seSolapaParcialmente() {
        RecursoPrestado prestamo = prestamoDeDiezADoce();

        assertTrue(prestamo.seSolapaCon(
                LocalDateTime.of(2026, 10, 1, 11, 0),
                LocalDateTime.of(2026, 10, 1, 13, 0)));
    }

    @Test
    @DisplayName("Se solapa cuando un rango contiene al otro")
    void seSolapaContenido() {
        RecursoPrestado prestamo = prestamoDeDiezADoce();

        assertTrue(prestamo.seSolapaCon(
                LocalDateTime.of(2026, 10, 1, 9, 0),
                LocalDateTime.of(2026, 10, 1, 14, 0)));
        assertTrue(prestamo.seSolapaCon(
                LocalDateTime.of(2026, 10, 1, 10, 30),
                LocalDateTime.of(2026, 10, 1, 11, 0)));
    }

    @Test
    @DisplayName("Tocarse justo en el borde NO cuenta como solapamiento")
    void noSeSolapaEnElBorde() {
        RecursoPrestado prestamo = prestamoDeDiezADoce();

        assertFalse(prestamo.seSolapaCon(DOCE, LocalDateTime.of(2026, 10, 1, 14, 0)));
        assertFalse(prestamo.seSolapaCon(LocalDateTime.of(2026, 10, 1, 8, 0), DIEZ));
    }

    @Test
    @DisplayName("No se solapa con rangos separados")
    void noSeSolapaSeparado() {
        RecursoPrestado prestamo = prestamoDeDiezADoce();

        assertFalse(prestamo.seSolapaCon(
                LocalDateTime.of(2026, 10, 1, 15, 0),
                LocalDateTime.of(2026, 10, 1, 16, 0)));
    }
}
