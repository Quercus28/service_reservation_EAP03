package com.service_reservation.EAP03.modulos.identidad.domain.exception;

import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class IdentityExceptionsTest {

    @Test
    @DisplayName("CredencialesInvalidasException conserva su mensaje")
    void invalidCredentials() {
        CredencialesInvalidasException ex = new CredencialesInvalidasException("Credenciales incorrectas");

        assertEquals("Credenciales incorrectas", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("CuentaBloqueadaException conserva su mensaje")
    void lockedAccount() {
        CuentaBloqueadaException ex = new CuentaBloqueadaException("Cuenta bloqueada");

        assertEquals("Cuenta bloqueada", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("ReglaNegocioException conserva su mensaje")
    void businessRule() {
        ReglaNegocioException ex = new ReglaNegocioException("El correo ya está en uso");

        assertEquals("El correo ya está en uso", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
