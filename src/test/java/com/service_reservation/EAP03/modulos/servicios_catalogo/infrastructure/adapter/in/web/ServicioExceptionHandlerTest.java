package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServicioExceptionHandlerTest {

    private final ServicioExceptionHandler handler = new ServicioExceptionHandler();

    @Test
    @DisplayName("El 404 de servicio usa el esquema {errorCode, message, traceId}")
    void notFoundUsesCommonSchema() {
        // Arrange
        ServicioNoEncontradoException ex = new ServicioNoEncontradoException(999999);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.manejarServicioNoEncontrado(ex);

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertEquals(List.of("errorCode", "message", "traceId"), List.copyOf(response.getBody().keySet()));
        assertEquals("SERVICIO_NO_ENCONTRADO", response.getBody().get("errorCode"));
        assertEquals("No se encontró el servicio con ID: 999999", response.getBody().get("message"));
        assertNotNull(response.getBody().get("traceId"));
    }

    @Test
    @DisplayName("El error de dominio de servicio usa el esquema {errorCode, message, traceId}")
    void domainErrorUsesCommonSchema() {
        // Arrange
        ServicioInvalidoException ex = new ServicioInvalidoException("La duración del servicio debe ser mayor a 0 minutos.");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.manejarDomainException(ex);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertEquals(List.of("errorCode", "message", "traceId"), List.copyOf(response.getBody().keySet()));
        assertEquals("SERVICIO_INVALIDO", response.getBody().get("errorCode"));
    }
}
