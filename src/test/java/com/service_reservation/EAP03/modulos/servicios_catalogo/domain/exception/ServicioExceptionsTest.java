package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioExceptionsTest {

    @Test
    @DisplayName("ServicioNoEncontradoException con id debe construir el mensaje con ese id")
    void noEncontradoConIdConstruyeMensaje() {
        // Act
        ServicioNoEncontradoException ex = new ServicioNoEncontradoException(42);

        // Assert
        assertEquals("No se encontró el servicio con ID: 42", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    @DisplayName("ServicioNoEncontradoException con mensaje debe usarlo tal cual")
    void noEncontradoConMensajePersonalizado() {
        // Act
        ServicioNoEncontradoException ex = new ServicioNoEncontradoException("El id del servicio es obligatorio");

        // Assert
        assertEquals("El id del servicio es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("ServicioInvalidoException debe ser una excepción de dominio no comprobada")
    void servicioInvalidoEsDomainException() {
        // Act
        ServicioInvalidoException ex = new ServicioInvalidoException("Dato inválido");

        // Assert
        assertEquals("Dato inválido", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("DomainException debe conservar la causa original")
    void domainExceptionConservaCausa() {
        // Arrange
        IllegalStateException causa = new IllegalStateException("origen");

        // Act
        DomainException ex = new DomainException("Fallo de dominio", causa) {
        };

        // Assert
        assertEquals("Fallo de dominio", ex.getMessage());
        assertSame(causa, ex.getCause());
    }
}
