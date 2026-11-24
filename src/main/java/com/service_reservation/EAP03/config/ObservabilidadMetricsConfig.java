package com.service_reservation.EAP03.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de métricas de negocio para Prometheus mediante Micrometer.
 */
@Configuration
public class ObservabilidadMetricsConfig {

    @Bean
    public Counter reservasCreadasCounter(MeterRegistry registry) {
        return Counter.builder("negocio.reservas.creadas.total")
                .description("Total de reservas creadas exitosamente")
                .register(registry);
    }

    @Bean
    public Counter reservasCanceladasCounter(MeterRegistry registry) {
        return Counter.builder("negocio.reservas.canceladas.total")
                .description("Total de reservas canceladas")
                .register(registry);
    }

    @Bean
    public Counter loginFallidosCounter(MeterRegistry registry) {
        return Counter.builder("negocio.auth.login.fallidos.total")
                .description("Total de intentos de inicio de sesion fallidos")
                .register(registry);
    }
}
