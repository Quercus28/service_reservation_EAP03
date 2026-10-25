package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolOperativoTest {

    @Test
    @DisplayName("La autoridad es el nombre del rol con el prefijo ROLE_")
    void authorityHasRolePrefix() {
        assertEquals("ROLE_CLIENTE", RolOperativo.CLIENTE.authority());
        assertEquals("ROLE_PROVEEDOR", RolOperativo.PROVEEDOR.authority());
    }

    @ParameterizedTest(name = "valor = [{0}]")
    @ValueSource(strings = {"PROVEEDOR", "proveedor", " ROLE_PROVEEDOR ", "role_proveedor"})
    @DisplayName("Convierte el texto ignorando mayúsculas, espacios y el prefijo ROLE_")
    void parsesRole(String valor) {
        assertEquals(RolOperativo.PROVEEDOR, RolOperativo.desde(valor));
    }

    @ParameterizedTest(name = "valor = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("El rol es obligatorio")
    void roleIsRequired(String valor) {
        DatosAdminInvalidosException ex = assertThrows(DatosAdminInvalidosException.class, () -> RolOperativo.desde(valor));

        assertEquals("El rol es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza un rol que no admite permisos operativos")
    void rejectsUnknownRole() {
        DatosAdminInvalidosException ex = assertThrows(DatosAdminInvalidosException.class, () -> RolOperativo.desde("ADMIN"));

        assertEquals("Rol no válido para permisos operativos: ADMIN", ex.getMessage());
    }
}
