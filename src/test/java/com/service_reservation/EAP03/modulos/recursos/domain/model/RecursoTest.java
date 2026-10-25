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
    @DisplayName("Rechaza nombre o precio nulos")
    void rejectsNullNameOrPrice() {
        assertThrows(DatosInvalidosException.class, () -> Recurso.nuevo(1, null, PRECIO, 3));
        assertThrows(DatosInvalidosException.class, () -> Recurso.nuevo(1, "Camilla", null, 3));
    }

    @Test
    @DisplayName("Un recurso nuevo tiene fecha de creación y conserva su precio")
    void newResourceHasCreationDateAndPrice() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);

        assertNotNull(recurso.getCreatedAt());
        assertNull(recurso.getEliminadoEn());
        assertEquals(PRECIO, recurso.getPrecioUnitario());
    }

    @Test
    @DisplayName("actualizarDatos cambia nombre, precio y stock")
    void updatesData() {
        // Arrange
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);

        // Act
        recurso.actualizarDatos("Camilla grande", BigDecimal.ONE, 7);

        // Assert
        assertEquals("Camilla grande", recurso.getNombre());
        assertEquals(BigDecimal.ONE, recurso.getPrecioUnitario());
        assertEquals(7, recurso.getStock());
    }

    @Test
    @DisplayName("actualizarDatos rechaza un precio negativo")
    void updateRejectsNegativePrice() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);

        BigDecimal negativo = new BigDecimal("-1");
        assertThrows(DatosInvalidosException.class, () -> recurso.actualizarDatos("Camilla", negativo, 3));
    }

    @Test
    @DisplayName("Crea un recurso valido y queda activo")
    void creaRecursoValido() {
        Recurso recurso = Recurso.nuevo(1, "Camilla 1", PRECIO, 3);

        assertNull(recurso.getId());
        assertEquals(1, recurso.getIdProveedor());
        assertEquals("Camilla 1", recurso.getNombre());
        assertEquals(3, recurso.getStock());
        assertTrue(recurso.estaActivo());
    }

    @Test
    @DisplayName("Rechaza nombre vacio")
    void rechazaNombreVacio() {
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1, "   ", PRECIO, 3));
    }

    @Test
    @DisplayName("Rechaza precio negativo")
    void rechazaPrecioNegativo() {
        BigDecimal negativo = new BigDecimal("-1");
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1, "Camilla", negativo, 3));
    }

    @Test
    @DisplayName("Rechaza stock negativo")
    void rechazaStockNegativo() {
        assertThrows(DatosInvalidosException.class,
                () -> Recurso.nuevo(1, "Camilla", PRECIO, -1));
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
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 5);
        assertEquals(3, recurso.unidadesDisponibles(2));
    }

    @Test
    @DisplayName("unidadesDisponibles nunca devuelve negativo")
    void unidadesDisponiblesNuncaNegativo() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 2);
        assertEquals(0, recurso.unidadesDisponibles(7));
    }

    @Test
    @DisplayName("Desactivar marca la fecha y deja de estar activo")
    void desactivar() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);
        recurso.desactivar();

        assertFalse(recurso.estaActivo());
        assertNotNull(recurso.getEliminadoEn());
    }

    @Test
    @DisplayName("No se puede desactivar dos veces")
    void noSeDesactivaDosVeces() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);
        recurso.desactivar();

        assertThrows(DatosInvalidosException.class, recurso::desactivar);
    }

    @Test
    @DisplayName("actualizarDatos valida igual que el constructor")
    void actualizarDatosValida() {
        Recurso recurso = Recurso.nuevo(1, "Camilla", PRECIO, 3);

        assertThrows(DatosInvalidosException.class,
                () -> recurso.actualizarDatos("", PRECIO, 3));
        assertThrows(DatosInvalidosException.class,
                () -> recurso.actualizarDatos("Camilla", PRECIO, -5));
    }

    @Test
    @DisplayName("Reconstruir conserva el estado desactivado")
    void reconstruirConservaDesactivacion() {
        LocalDateTime borrado = LocalDateTime.of(2026, 9, 1, 10, 0);
        Recurso recurso = Recurso.reconstruir(9, 1, "Camilla", PRECIO, 3,
                LocalDateTime.of(2026, 8, 1, 10, 0), borrado);

        assertFalse(recurso.estaActivo());
        assertEquals(borrado, recurso.getEliminadoEn());
    }
}
