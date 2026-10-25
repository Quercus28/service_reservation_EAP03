package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class UsuarioNoEsClienteExceptionTest {

    @Test
    @DisplayName("Indica que el usuario no tiene perfil de cliente")
    void buildsMessage() {
        UsuarioNoEsClienteException ex = new UsuarioNoEsClienteException();

        assertEquals("El usuario autenticado no tiene un perfil de cliente asociado.", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
