package com.service_reservation.EAP03.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.EnumFeature;

/**
 * Por defecto Jackson acepta un número como posición del enum: {@code "diaSemana": 0} se lee como
 * LUNES. Con esto un enum solo se puede enviar por su nombre, también si el número va como texto.
 * Se configura aquí y no en application.yaml para que aplique igual en las pruebas, que usan su
 * propio application.yaml.
 */
@Configuration
public class EnumsSoloPorNombreConfig {

    @Bean
    public JsonMapperBuilderCustomizer rechazarNumerosEnEnums() {
        return builder -> builder.enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
    }
}
