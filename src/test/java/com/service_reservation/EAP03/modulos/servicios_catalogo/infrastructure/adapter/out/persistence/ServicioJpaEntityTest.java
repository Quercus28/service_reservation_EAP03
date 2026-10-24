package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioJpaEntityTest {

    @Test
    @DisplayName("prePersist debe asignar la fecha de creación cuando no existe")
    void prePersistAsignaFechaDeCreacion() {
        // Arrange
        ServicioJpaEntity entity = new ServicioJpaEntity();
        OffsetDateTime antes = OffsetDateTime.now();

        // Act
        entity.prePersist();

        // Assert
        assertNotNull(entity.getCreatedAt());
        assertFalse(entity.getCreatedAt().isBefore(antes));
    }

    @Test
    @DisplayName("prePersist no debe sobrescribir una fecha de creación existente")
    void prePersistConservaFechaExistente() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        ServicioJpaEntity entity = new ServicioJpaEntity(1, 2, 30, "Corte", creado);

        // Act
        entity.prePersist();

        // Assert
        assertEquals(creado, entity.getCreatedAt());
    }

    @Test
    @DisplayName("Los setters deben actualizar todos los campos")
    void settersActualizanCampos() {
        // Arrange
        ServicioJpaEntity entity = new ServicioJpaEntity();
        OffsetDateTime creado = OffsetDateTime.now();

        // Act
        entity.setId(4);
        entity.setIdProveedor(8);
        entity.setNombre("Tinte");
        entity.setDuracionMinutos(90);
        entity.setCreatedAt(creado);

        // Assert
        assertEquals(4, entity.getId());
        assertEquals(8, entity.getIdProveedor());
        assertEquals("Tinte", entity.getNombre());
        assertEquals(90, entity.getDuracionMinutos());
        assertEquals(creado, entity.getCreatedAt());
    }

    @Test
    @DisplayName("La igualdad y el hashCode dependen solo del id")
    void igualdadPorId() {
        // Arrange
        ServicioJpaEntity a = new ServicioJpaEntity(1, 2, 30, "Corte", null);
        ServicioJpaEntity b = new ServicioJpaEntity(1, 9, 60, "Tinte", null);
        ServicioJpaEntity c = new ServicioJpaEntity(2, 2, 30, "Corte", null);

        // Assert
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(null, a);
        assertNotEquals("Corte", a);
    }
}
