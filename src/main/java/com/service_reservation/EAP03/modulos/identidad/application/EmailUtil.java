package com.service_reservation.EAP03.modulos.identidad.application;

import java.util.Locale;

/**
 * Forma única de comparar y guardar correos: sin espacios y en minúsculas.
 * La usan el registro y el login para que no traten distinto un mismo correo.
 */
public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalizarEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
