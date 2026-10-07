package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;

import java.util.Locale;

/**
 * Roles sobre los que el administrador puede encender o revocar permisos operativos.
 * ROLE_ADMIN queda fuera de forma intencional: no se puede revocar desde este módulo.
 */
public enum RolOperativo {
    CLIENTE,
    PROVEEDOR;

    /** Nombre del rol tal como está persistido en la tabla ROL. */
    public String authority() {
        return "ROLE_" + name();
    }

    public static RolOperativo desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DatosAdminInvalidosException("El rol es obligatorio");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        if (normalizado.startsWith("ROLE_")) {
            normalizado = normalizado.substring("ROLE_".length());
        }
        try {
            return RolOperativo.valueOf(normalizado);
        } catch (IllegalArgumentException ex) {
            throw new DatosAdminInvalidosException("Rol no válido para permisos operativos: " + valor);
        }
    }
}
