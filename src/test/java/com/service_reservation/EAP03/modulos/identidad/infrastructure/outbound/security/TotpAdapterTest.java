package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TotpAdapterTest {

    private final TotpAdapter totpAdapter = new TotpAdapter();

    private static String currentCode(String secreto) throws Exception {
        long periodo = Math.floorDiv(System.currentTimeMillis() / 1000, 30);
        return new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6).generate(secreto, periodo);
    }

    @Test
    @DisplayName("Genera secretos distintos en cada llamada")
    void generatesSecrets() {
        // Act
        String primero = totpAdapter.generarSecreto();
        String segundo = totpAdapter.generarSecreto();

        // Assert
        assertFalse(primero.isBlank());
        assertNotEquals(primero, segundo);
    }

    @Test
    @DisplayName("Genera el código QR como imagen PNG")
    void generatesPngQr() {
        // Act
        byte[] qr = totpAdapter.generarQrPng("admin@test.com", totpAdapter.generarSecreto());

        // Assert
        assertTrue(qr.length > 8);
        assertEquals((byte) 0x89, qr[0]);
        assertEquals('P', qr[1]);
        assertEquals('N', qr[2]);
        assertEquals('G', qr[3]);
    }

    @Test
    @DisplayName("Envuelve el error de la librería al generar el QR")
    void wrapsQrGenerationError() throws Exception {
        // Arrange
        QrGenerator generadorQueFalla = mock(QrGenerator.class);
        QrGenerationException causa = new QrGenerationException("fallo", null);
        when(generadorQueFalla.generate(any())).thenThrow(causa);
        Field campo = TotpAdapter.class.getDeclaredField("qrGenerator");
        campo.setAccessible(true);
        campo.set(totpAdapter, generadorQueFalla);

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> totpAdapter.generarQrPng("admin@test.com", "SECRETO"));

        // Assert
        assertEquals("Error al generar el código QR para 2FA", ex.getMessage());
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("Acepta el código vigente aunque tenga espacios alrededor")
    void acceptsCurrentCode() throws Exception {
        // Arrange
        String secreto = totpAdapter.generarSecreto();
        String codigo = currentCode(secreto);

        // Act & Assert
        assertTrue(totpAdapter.validarCodigo(secreto, " " + codigo + " "));
    }

    @Test
    @DisplayName("Rechaza un código incorrecto")
    void rejectsWrongCode() throws Exception {
        // Arrange
        String secreto = totpAdapter.generarSecreto();
        String codigo = currentCode(secreto);
        String incorrecto = codigo.equals("000000") ? "111111" : "000000";

        // Act & Assert
        assertFalse(totpAdapter.validarCodigo(secreto, incorrecto));
    }

    @Test
    @DisplayName("Rechaza la validación si falta el secreto o el código")
    void rejectsNullSecretOrCode() {
        assertFalse(totpAdapter.validarCodigo(null, "123456"));
        assertFalse(totpAdapter.validarCodigo("SECRETO", null));
    }
}
