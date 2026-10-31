package com.service_reservation.EAP03;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String nombreSeguridad = "JWT-Auth";

        return new OpenAPI()
                .info(new Info()
                        .title("API de Reservas de Servicios")
                        .version("1.0.0")
                        .description("API para la gestión de reservas de servicios, incluyendo disponibilidad, clientes, solicitudes y procesos operativos del sistema."))
                .addSecurityItem(new SecurityRequirement().addList(nombreSeguridad))
                .components(new Components()
                        .addSecuritySchemes(nombreSeguridad, new SecurityScheme()
                                .name(nombreSeguridad)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }
}
