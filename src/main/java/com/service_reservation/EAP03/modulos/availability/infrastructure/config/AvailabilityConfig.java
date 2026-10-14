package com.service_reservation.EAP03.modulos.availability.infrastructure.config;

import com.service_reservation.EAP03.modulos.availability.domain.service.DisponibilidadCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AvailabilityConfig {

    @Bean
    public DisponibilidadCalculator disponibilidadCalculator() {
        return new DisponibilidadCalculator();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
