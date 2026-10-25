package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioRecursoTest {

    @Test
    @DisplayName("Crea una asociación nueva sin id")
    void createsNewLink() {
        ServicioRecurso asociacion = ServicioRecurso.nuevo(10, 3, 2);

        assertNull(asociacion.getId());
        assertEquals(10, asociacion.getIdServicio());
        assertEquals(3, asociacion.getIdRecurso());
        assertEquals(2, asociacion.getCantidad());
    }

    @Test
    @DisplayName("Reconstruye una asociación existente con su id")
    void rebuildsLink() {
        assertEquals(4, ServicioRecurso.reconstruir(4, 10, 3, 1).getId());
    }

    @Test
    @DisplayName("Rechaza una cantidad menor o igual a cero")
    void rejectsInvalidQuantity() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> ServicioRecurso.nuevo(10, 3, 0));

        assertEquals("La cantidad debe ser mayor que cero", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza una asociación sin servicio o sin recurso")
    void rejectsNullIds() {
        assertThrows(NullPointerException.class, () -> ServicioRecurso.nuevo(null, 3, 1));
        assertThrows(NullPointerException.class, () -> ServicioRecurso.nuevo(10, null, 1));
    }
}
