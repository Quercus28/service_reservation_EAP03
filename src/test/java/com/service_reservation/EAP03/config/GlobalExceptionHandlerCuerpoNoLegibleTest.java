package com.service_reservation.EAP03.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlobalExceptionHandlerCuerpoNoLegibleTest {

    private enum Color { ROJO, VERDE }

    private enum Talla {
        PEQUENA;

        @Override
        public String toString() {
            return "S";
        }
    }

    private record Item(Color color) { }

    private record Pedido(List<Item> items, Integer cantidad, Talla talla) { }

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

    private Map<String, Object> respuestaPara(String json, Class<?> tipo) {
        RuntimeException causa = assertThrows(RuntimeException.class, () -> MAPPER.readValue(json, tipo));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "error", causa, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<Map<String, Object>> response =
                handler.handleCuerpoNoLegible(ex, new MockHttpServletRequest("POST", "/api/v1/pedidos"));

        assertEquals(400, response.getStatusCode().value());
        return response.getBody();
    }

    @Test
    @DisplayName("Funciona con cualquier enum y nombra el campo dentro de una lista")
    void anyEnumReportsNestedField() {
        Map<String, Object> body = respuestaPara("{\"items\": [{\"color\": \"AZUL\"}]}", Pedido.class);

        assertEquals("VALIDACION_ENTRADA", body.get("errorCode"));
        assertEquals(Map.of("items[0].color", "El valor enviado no es válido. Valores permitidos: ROJO, VERDE"),
                body.get("details"));
    }

    @Test
    @DisplayName("Lista los valores que el enum acepta de verdad al leer el JSON")
    void listsValuesAcceptedByReader() {
        Map<String, Object> body = respuestaPara("{\"talla\": \"PEQUENA\"}", Pedido.class);

        assertEquals(Map.of("talla", "El valor enviado no es válido. Valores permitidos: S"), body.get("details"));
    }

    @Test
    @DisplayName("Un valor no convertible que no es enum nombra el campo sin lista de valores")
    void nonEnumValueReportsFieldOnly() {
        Map<String, Object> body = respuestaPara("{\"cantidad\": \"muchos\"}", Pedido.class);

        assertEquals("VALIDACION_ENTRADA", body.get("errorCode"));
        assertEquals(Map.of("cantidad", "El valor enviado no es válido"), body.get("details"));
    }

    @Test
    @DisplayName("Si el valor inválido no está en ningún campo sigue respondiendo CUERPO_NO_VALIDO")
    void rootValueKeepsBodyError() {
        Map<String, Object> body = respuestaPara("\"AZUL\"", Color.class);

        assertEquals("CUERPO_NO_VALIDO", body.get("errorCode"));
        assertFalse(body.containsKey("details"));
    }

    @Test
    @DisplayName("Un campo desconocido sigue respondiendo CUERPO_NO_VALIDO")
    void unknownFieldKeepsBodyError() {
        Map<String, Object> body = respuestaPara("{\"otro\": 1}", Pedido.class);

        assertEquals("CUERPO_NO_VALIDO", body.get("errorCode"));
        assertFalse(body.containsKey("details"));
    }
}
