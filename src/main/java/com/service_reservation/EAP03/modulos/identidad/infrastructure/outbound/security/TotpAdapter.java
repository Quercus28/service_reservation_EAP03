package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.TotpPort;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Component;

@Component
public class TotpAdapter implements TotpPort {

    private static final String ISSUER = "EAP03";
    private static final int DISCREPANCY = 1; // Permite +/- 30 segundos de tolerancia

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public TotpAdapter() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setAllowedTimePeriodDiscrepancy(DISCREPANCY);
        this.codeVerifier = verifier;
    }

    @Override
    public String generarSecreto() {
        return secretGenerator.generate();
    }

    @Override
    public byte[] generarQrPng(String email, String secret) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            return qrGenerator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException("Error al generar el código QR para 2FA", e);
        }
    }

    @Override
    public boolean validarCodigo(String secret, String codigo) {
        if (secret == null || codigo == null) {
            return false;
        }
        return codeVerifier.isValidCode(secret, codigo.trim());
    }
}
