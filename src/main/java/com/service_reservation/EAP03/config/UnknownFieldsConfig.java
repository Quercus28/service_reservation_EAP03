package com.service_reservation.EAP03.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Spring Boot desactiva FAIL_ON_UNKNOWN_PROPERTIES para toda la aplicación, y
 * {@code @JsonIgnoreProperties(ignoreUnknown = false)} no lo vuelve a activar por clase.
 * Este handler hace que los DTO marcados con {@link RejectUnknownFields} sí fallen
 * ante un campo desconocido, sin cambiar el comportamiento del resto de endpoints.
 */
@Configuration
public class UnknownFieldsConfig {

    @Bean
    public JsonMapperBuilderCustomizer rejectUnknownFields() {
        return builder -> builder.addHandler(new RejectUnknownFieldsHandler());
    }

    static class RejectUnknownFieldsHandler extends DeserializationProblemHandler {

        @Override
        public boolean handleUnknownProperty(
                DeserializationContext ctxt,
                JsonParser p,
                ValueDeserializer<?> deserializer,
                Object beanOrClass,
                String propertyName
        ) throws JacksonException {
            // En los records Jackson pasa la clase, porque el objeto aún no se ha construido.
            Class<?> type = beanOrClass instanceof Class<?> clazz ? clazz : beanOrClass.getClass();
            if (type.isAnnotationPresent(RejectUnknownFields.class)) {
                throw UnrecognizedPropertyException.from(p, beanOrClass, propertyName,
                        deserializer != null ? deserializer.getKnownPropertyNames() : null);
            }
            return false;
        }
    }
}
