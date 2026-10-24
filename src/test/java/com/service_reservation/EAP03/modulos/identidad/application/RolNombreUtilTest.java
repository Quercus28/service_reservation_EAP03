package com.service_reservation.EAP03.modulos.identidad.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RolNombreUtilTest {

    @Test
    void addsRolePrefix() {
        assertEquals("ROLE_CLIENTE", RolNombreUtil.normalizarRol("CLIENTE"));
        assertEquals("ROLE_PROVEEDOR", RolNombreUtil.normalizarRol("PROVEEDOR"));
        assertEquals("ROLE_ADMIN", RolNombreUtil.normalizarRol("ADMIN"));
        assertEquals("ROLE_CLIENTE", RolNombreUtil.normalizarRol("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Recorta los espacios del rol antes de normalizarlo")
    void trimsSpaces() {
        assertEquals("ROLE_CLIENTE", RolNombreUtil.normalizarRol("  CLIENTE "));
        assertEquals("ROLE_ADMIN", RolNombreUtil.normalizarRol(" ROLE_ADMIN "));
    }

    @Test
    @DisplayName("Devuelve null cuando el rol es nulo")
    void returnsNullForNullRole() {
        assertNull(RolNombreUtil.normalizarRol(null));
    }
}
