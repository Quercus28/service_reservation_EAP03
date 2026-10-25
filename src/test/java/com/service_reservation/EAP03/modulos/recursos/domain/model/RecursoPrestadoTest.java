package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RecursoPrestadoTest {

    private static final BigDecimal PRECIO = new BigDecimal("100000.00");

    @Test
    @DisplayName("Reconstruye un préstamo con todos sus datos")
    void rebuildsLoan() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime fin = inicio.plusHours(2);
        LocalDateTime liberado = inicio.minusDays(1);

        // Act
        RecursoPrestado prestamo = RecursoPrestado.reconstruir(4, 1, 100, 2, inicio, fin, PRECIO, liberado);

        // Assert
        assertEquals(4, prestamo.getId());
        assertEquals(1, prestamo.getIdRecurso());
        assertEquals(100, prestamo.getIdServicioPrestado());
        assertEquals(fin, prestamo.getFechaFin());
        assertEquals(PRECIO, prestamo.getPrecioTotal());
        assertEquals(liberado, prestamo.getLiberadoEn());
        assertTrue(prestamo.estaLiberado());
    }

    @Test
    @DisplayName("Liberar marca la fecha una sola vez")
    void releasesOnlyOnce() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 10, 0);
        RecursoPrestado prestamo = RecursoPrestado.nuevo(1, 100, 2, inicio, inicio.plusHours(2), PRECIO);
        LocalDateTime primero = inicio.minusDays(2);

        // Act
        assertFalse(prestamo.estaLiberado());
        prestamo.liberar(primero);
        prestamo.liberar(inicio.minusDays(1));

        // Assert
        assertTrue(prestamo.estaLiberado());
        assertEquals(primero, prestamo.getLiberadoEn());
    }

    @Test
    @DisplayName("Liberar exige la fecha actual")
    void releaseRequiresCurrentDate() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 10, 0);
        RecursoPrestado prestamo = RecursoPrestado.nuevo(1, 100, 2, inicio, inicio.plusHours(2), PRECIO);

        assertThrows(NullPointerException.class, () -> prestamo.liberar(null));
    }

    @Test
    @DisplayName("Rechaza fechas, precio, recurso o reserva nulos")
    void rejectsNullValues() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime fin = inicio.plusHours(2);

        assertThrows(DatosInvalidosException.class, () -> RecursoPrestado.nuevo(1, 100, 2, null, fin, PRECIO));
        assertThrows(DatosInvalidosException.class, () -> RecursoPrestado.nuevo(1, 100, 2, inicio, null, PRECIO));
        assertThrows(DatosInvalidosException.class, () -> RecursoPrestado.nuevo(1, 100, 2, inicio, fin, null));
        assertThrows(NullPointerException.class, () -> RecursoPrestado.nuevo(null, 100, 2, inicio, fin, PRECIO));
        assertThrows(NullPointerException.class, () -> RecursoPrestado.nuevo(1, null, 2, inicio, fin, PRECIO));
    }
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
