package com.service_reservation.EAP03.modulos.identidad.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RolNombreUtilTest {

    @Test
    void normalizaRolesConPrefijoRole() {
        assertEquals("ROLE_CLIENTE", RolNombreUtil.normalizarRol("CLIENTE"));
        assertEquals("ROLE_PROVEEDOR", RolNombreUtil.normalizarRol("PROVEEDOR"));
        assertEquals("ROLE_ADMIN", RolNombreUtil.normalizarRol("ADMIN"));
        assertEquals("ROLE_CLIENTE", RolNombreUtil.normalizarRol("ROLE_CLIENTE"));
    }
}
