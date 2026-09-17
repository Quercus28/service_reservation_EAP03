package com.service_reservation.EAP03.modulos.identidad.application;

public final class RolNombreUtil {

    private RolNombreUtil() {
    }

    public static String normalizarRol(String nombreRol) {
        if (nombreRol == null) {
            return null;
        }

        String rol = nombreRol.trim();
        if (rol.startsWith("ROLE_")) {
            return rol;
        }

        return "ROLE_" + rol;
    }
}
