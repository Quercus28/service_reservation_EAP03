package com.service_reservation.EAP03.modulos.identidad.domain.model;

import java.util.Arrays;
import java.util.Objects;

public record ResultadoConfiguracion2fa(
    byte[] imagenQr,
    String secreto
) {
    @Override
    public boolean equals(Object o) {
        return o instanceof ResultadoConfiguracion2fa(byte[] otraImagen, String otroSecreto)
                && Arrays.equals(imagenQr, otraImagen)
                && Objects.equals(secreto, otroSecreto);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(imagenQr) + Objects.hashCode(secreto);
    }

    @Override
    public String toString() {
        return "ResultadoConfiguracion2fa[imagenQr=" + Arrays.toString(imagenQr) + ", secreto=" + secreto + "]";
    }
}
