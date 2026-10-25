package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @RejectUnknownFields
    static class AnnotatedBean {
    }

    static class UnannotatedBean {
    }

    @Test
    @DisplayName("Rechaza un campo desconocido cuando recibe la instancia de un DTO anotado")
    void rejectsUnknownFieldOnAnnotatedInstance() {
        // Arrange
        UnknownFieldsConfig.RejectUnknownFieldsHandler handler = new UnknownFieldsConfig.RejectUnknownFieldsHandler();
        JsonParser parser = mapper.createParser("{}");
        AnnotatedBean bean = new AnnotatedBean();

        // Act
        UnrecognizedPropertyException ex = assertThrows(UnrecognizedPropertyException.class,
                () -> handler.handleUnknownProperty(null, parser, null, bean, "extra"));

        // Assert
        assertEquals("extra", ex.getPropertyName());
    }

    @Test
    @DisplayName("Informa los campos conocidos del deserializador al rechazar un campo desconocido")
    void reportsKnownPropertiesFromDeserializer() {
        // Arrange
        UnknownFieldsConfig.RejectUnknownFieldsHandler handler = new UnknownFieldsConfig.RejectUnknownFieldsHandler();
        JsonParser parser = mapper.createParser("{}");
        ValueDeserializer<?> deserializer = mock(ValueDeserializer.class);
        when(deserializer.getKnownPropertyNames()).thenReturn(List.of("nombre"));

        // Act
        UnrecognizedPropertyException ex = assertThrows(UnrecognizedPropertyException.class,
                () -> handler.handleUnknownProperty(null, parser, deserializer, AnnotatedBean.class, "extra"));

        // Assert
        assertEquals(List.of("nombre"), List.copyOf(ex.getKnownPropertyIds()));
    }

    @Test
    @DisplayName("Ignora un campo desconocido cuando recibe la instancia de un DTO sin anotar")
    void ignoresUnknownFieldOnUnannotatedInstance() {
        // Arrange
        UnknownFieldsConfig.RejectUnknownFieldsHandler handler = new UnknownFieldsConfig.RejectUnknownFieldsHandler();
        JsonParser parser = mapper.createParser("{}");

        // Act
        boolean manejado = handler.handleUnknownProperty(null, parser, null, new UnannotatedBean(), "extra");

        // Assert
        assertFalse(manejado);
    }

    @Test
    @DisplayName("La configuración registra el handler en el constructor del JsonMapper")
    void configurationRegistersHandler() {
        // Arrange
        JsonMapper.Builder builder = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        new UnknownFieldsConfig().rejectUnknownFields().customize(builder);
        JsonMapper configurado = builder.build();
        String json = """
                {"nombre": "Corte", "duracionMinutos": 30, "capacidadMaxima": 10}
                """;

        // Act & Assert
        assertThrows(UnrecognizedPropertyException.class,
                () -> configurado.readValue(json, ActualizarServicioRequest.class));
    }
}
