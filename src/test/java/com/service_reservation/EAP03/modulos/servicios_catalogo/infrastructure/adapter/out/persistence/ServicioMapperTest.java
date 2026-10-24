package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServicioMapperTest {

    private final ServicioMapper mapper = new ServicioMapper();

    @Test
    @DisplayName("Debe convertir una entidad JPA en el modelo de dominio")
    void debeConvertirEntidadADominio() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.parse("2026-10-01T08:30:00-05:00");
        ServicioJpaEntity entity = new ServicioJpaEntity(7, 3, 60, "Masaje", creado);

        // Act
        Servicio servicio = mapper.toDomain(entity);

        // Assert
        assertEquals(7, servicio.getId());
        assertEquals(3, servicio.getIdProveedor());
        assertEquals("Masaje", servicio.getNombre());
        assertEquals(60, servicio.getDuracionMinutos());
        assertEquals(creado, servicio.getCreatedAt());
    }

    @Test
    @DisplayName("Debe convertir el modelo de dominio en una entidad JPA")
    void debeConvertirDominioAEntidad() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.parse("2026-10-01T08:30:00-05:00");
        Servicio servicio = Servicio.reconstituir(7, 3, "Masaje", 60, creado);

        // Act
        ServicioJpaEntity entity = mapper.toJpaEntity(servicio);

        // Assert
        assertEquals(7, entity.getId());
        assertEquals(3, entity.getIdProveedor());
        assertEquals("Masaje", entity.getNombre());
        assertEquals(60, entity.getDuracionMinutos());
        assertEquals(creado, entity.getCreatedAt());
    }

    @Test
    @DisplayName("Un servicio nuevo debe mapearse a una entidad sin id ni fecha de creación")
    void debeMapearServicioNuevoSinId() {
        // Act
        ServicioJpaEntity entity = mapper.toJpaEntity(Servicio.crearNuevo(3, "Masaje", 60));

        // Assert
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Debe devolver null al mapear valores nulos en ambos sentidos")
    void debeDevolverNullConEntradaNula() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toJpaEntity(null));
    }

    @Test
    @DisplayName("Una entidad sin id no puede reconstituirse como dominio")
    void entidadSinIdFalla() {
        // Arrange
        ServicioJpaEntity entity = new ServicioJpaEntity(null, 3, 60, "Masaje", null);

        // Act & Assert
        assertThrows(ServicioInvalidoException.class, () -> mapper.toDomain(entity));
    }
}
