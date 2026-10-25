package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoDeOtroProveedorExceptionTest {

    @Test
    @DisplayName("Indica que el recurso pertenece a otro proveedor")
    void buildsMessage() {
        RecursoDeOtroProveedorException ex = new RecursoDeOtroProveedorException(3, 10);

        assertEquals("El recurso 3 no se puede usar en el servicio 10 porque pertenece a otro proveedor.", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
