package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RecursoTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");

    @Test
    @DisplayName("Crea un recurso valido y queda activo")
    void creaRecursoValido() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla 1", PRECIO, 3);

        assertNull(recurso.getId());
        assertEquals(1L, recurso.getIdProveedor());
        assertEquals("Camilla 1", recurso.getNombre());
        assertEquals(3, recurso.getStock());
        assertTrue(recurso.estaActivo());
    }

    @Test
    @DisplayName("Rechaza nombre vacio")
    void rechazaNombreVacio() {
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1L, "   ", PRECIO, 3));
    }

    @Test
    @DisplayName("Rechaza precio negativo")
    void rechazaPrecioNegativo() {
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1L, "Camilla", new BigDecimal("-1"), 3));
    }

    @Test
    @DisplayName("Rechaza stock negativo")
    void rechazaStockNegativo() {
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1L, "Camilla", PRECIO, -1));
    }

    @Test
    @DisplayName("Rechaza proveedor nulo")
    void rechazaProveedorNulo() {
        assertThrows(NullPointerException.class,
                () -> Recurso.nuevo(null, "Camilla", PRECIO, 3));
    }

    @Test
    @DisplayName("unidadesDisponibles resta lo comprometido")
    void unidadesDisponiblesResta() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla", PRECIO, 5);
        assertEquals(3, recurso.unidadesDisponibles(2));
    }

    @Test
    @DisplayName("unidadesDisponibles nunca devuelve negativo")
    void unidadesDisponiblesNuncaNegativo() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla", PRECIO, 2);
        assertEquals(0, recurso.unidadesDisponibles(7));
    }

    @Test
    @DisplayName("Desactivar marca la fecha y deja de estar activo")
    void desactivar() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla", PRECIO, 3);
        recurso.desactivar();

        assertFalse(recurso.estaActivo());
        assertNotNull(recurso.getEliminadoEn());
    }

    @Test
    @DisplayName("No se puede desactivar dos veces")
    void noSeDesactivaDosVeces() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla", PRECIO, 3);
        recurso.desactivar();

        assertThrows(DatosInvalidosException.class, recurso::desactivar);
    }

    @Test
    @DisplayName("actualizarDatos valida igual que el constructor")
    void actualizarDatosValida() {
        Recurso recurso = Recurso.nuevo(1L, "Camilla", PRECIO, 3);

        assertThrows(DatosInvalidosException.class,
                () -> recurso.actualizarDatos("", PRECIO, 3));
        assertThrows(DatosInvalidosException.class,
                () -> recurso.actualizarDatos("Camilla", PRECIO, -5));
    }

    @Test
    @DisplayName("Reconstruir conserva el estado desactivado")
    void reconstruirConservaDesactivacion() {
        LocalDateTime borrado = LocalDateTime.of(2026, 9, 1, 10, 0);
        Recurso recurso = Recurso.reconstruir(9, 1L, "Camilla", PRECIO, 3,
                LocalDateTime.of(2026, 8, 1, 10, 0), borrado);

        assertFalse(recurso.estaActivo());
        assertEquals(borrado, recurso.getEliminadoEn());
    }
}
