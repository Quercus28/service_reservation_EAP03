package com.service_reservation.EAP03.modulos.identidad.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailUtilTest {

    @Test
    @DisplayName("Normaliza el correo quitando espacios y pasándolo a minúsculas")
    void normalizesEmail() {
        assertEquals("usuario@test.com", EmailUtil.normalizarEmail("  Usuario@TEST.com "));
    }

    @Test
    @DisplayName("Devuelve null cuando el correo es nulo")
    void returnsNullForNullEmail() {
        assertNull(EmailUtil.normalizarEmail(null));
    }
}
