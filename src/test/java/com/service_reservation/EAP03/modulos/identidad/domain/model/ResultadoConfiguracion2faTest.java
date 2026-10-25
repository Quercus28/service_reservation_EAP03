package com.service_reservation.EAP03.modulos.identidad.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoConfiguracion2faTest {

    private static final ResultadoConfiguracion2fa RESULTADO = new ResultadoConfiguracion2fa(new byte[]{1, 2, 3}, "SECRETO");

    @Test
    @DisplayName("Dos resultados con la misma imagen y el mismo secreto son iguales aunque los arreglos sean distintos")
    void equalByContent() {
        ResultadoConfiguracion2fa otro = new ResultadoConfiguracion2fa(new byte[]{1, 2, 3}, "SECRETO");

        assertEquals(RESULTADO, otro);
        assertEquals(RESULTADO.hashCode(), otro.hashCode());
    }

    @Test
    @DisplayName("No son iguales si cambia la imagen, el secreto o el tipo de objeto")
    void differentContentIsNotEqual() {
        assertNotEquals(RESULTADO, new ResultadoConfiguracion2fa(new byte[]{9}, "SECRETO"));
        assertNotEquals(RESULTADO, new ResultadoConfiguracion2fa(new byte[]{1, 2, 3}, "OTRO"));
        assertFalse(RESULTADO.equals(null));
        assertFalse(RESULTADO.equals("SECRETO"));
    }

    @Test
    @DisplayName("toString muestra el contenido de la imagen y el secreto")
    void toStringShowsContent() {
        assertEquals("ResultadoConfiguracion2fa[imagenQr=[1, 2, 3], secreto=SECRETO]", RESULTADO.toString());
    }
}
