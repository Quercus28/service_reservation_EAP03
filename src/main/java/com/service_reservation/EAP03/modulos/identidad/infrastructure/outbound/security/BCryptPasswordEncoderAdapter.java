package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder springPasswordEncoder;

    public BCryptPasswordEncoderAdapter(PasswordEncoder springPasswordEncoder) {
        this.springPasswordEncoder = springPasswordEncoder;
    }

    @Override
    public String codificar(String passwordPlana) {
        return springPasswordEncoder.encode(passwordPlana);
    }

    @Override
    public boolean coincide(String passwordPlana, String passwordCodificada) {
        return springPasswordEncoder.matches(passwordPlana, passwordCodificada);
    }
}