package com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioResponseTest {

    @Test
    @DisplayName("Debe mapear todos los campos desde el modelo de dominio")
    void debeMapearDesdeDominio() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.parse("2026-10-01T08:30:00-05:00");
        Servicio servicio = Servicio.reconstituir(7, 3, "Masaje", 60, creado);

        // Act
        ServicioResponse response = ServicioResponse.fromDomain(servicio);

        // Assert
        assertEquals(new ServicioResponse(7, 3, "Masaje", 60, creado), response);
    }

    @Test
    @DisplayName("Debe devolver null cuando el servicio de dominio es nulo")
    void debeDevolverNullConDominioNulo() {
        assertNull(ServicioResponse.fromDomain(null));
    }
}
