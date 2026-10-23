package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class UnknownFieldsConfigTest {
    record UnannotatedDto(String nombre) {
    }

    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addHandler(new UnknownFieldsConfig.RejectUnknownFieldsHandler())
                .build();
    }

    @Test
    @DisplayName("Rechaza capacidadMaxima en el alta de servicio")
    void rejectsCapacidadMaximaOnCreate() {
        // Arrange
        String json = """
                {"idProveedor": 1, "nombre": "Corte", "duracionMinutos": 30, "capacidadMaxima": 10}
                """;

        // Act
        UnrecognizedPropertyException ex = assertThrows(UnrecognizedPropertyException.class,
                () -> mapper.readValue(json, CrearServicioRequest.class));

        // Assert
        assertEquals("capacidadMaxima", ex.getPropertyName());
    }

    @Test
    @DisplayName("Rechaza un campo desconocido al actualizar un servicio")
    void rejectsUnknownFieldOnUpdate() {
        // Arrange
        String json = """
                {"nombre": "Corte", "duracionMinutos": 30, "capacidadMaxima": 10}
                """;

        // Act
        UnrecognizedPropertyException ex = assertThrows(UnrecognizedPropertyException.class,
                () -> mapper.readValue(json, ActualizarServicioRequest.class));

        // Assert
        assertEquals("capacidadMaxima", ex.getPropertyName());
    }

    @Test
    @DisplayName("Acepta el alta de servicio cuando solo trae campos conocidos")
    void acceptsCreateWithKnownFields() {
        // Arrange
        String json = """
                {"idProveedor": 1, "nombre": "Corte", "duracionMinutos": 30}
                """;

        // Act
        CrearServicioRequest request = mapper.readValue(json, CrearServicioRequest.class);

        // Assert
        assertEquals(new CrearServicioRequest(1, "Corte", 30), request);
    }

    @Test
    @DisplayName("Un DTO sin la anotación sigue ignorando los campos desconocidos")
    void unannotatedDtoIgnoresUnknownFields() {
        // Arrange
        String json = """
                {"nombre": "Corte", "extra": true}
                """;

        // Act
        UnannotatedDto dto = mapper.readValue(json, UnannotatedDto.class);

        // Assert
        assertEquals("Corte", dto.nombre());
    }
}
