package com.service_reservation.EAP03.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/servicios");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private String traceIdSeenByChain() throws Exception {
        AtomicReference<String> visto = new AtomicReference<>();
        FilterChain chain = (req, res) -> visto.set(MDC.get(TraceIdFilter.CLAVE_MDC));
        filter.doFilterInternal(request, response, chain);
        return visto.get();
    }

    @Test
    @DisplayName("Reutiliza en minúsculas el traceId de la cabecera cuando es un UUID válido")
    void reusesValidHeaderInLowercase() throws Exception {
        // Arrange
        request.addHeader(TraceIdFilter.CABECERA, "3F2504E0-4F89-11D3-9A0C-0305E82C3301");

        // Act
        String traceId = traceIdSeenByChain();

        // Assert
        assertEquals("3f2504e0-4f89-11d3-9a0c-0305e82c3301", traceId);
        assertEquals(traceId, response.getHeader(TraceIdFilter.CABECERA));
    }

    @Test
    @DisplayName("Genera un traceId nuevo cuando no llega cabecera")
    void generatesTraceIdWithoutHeader() throws Exception {
        // Act
        String traceId = traceIdSeenByChain();

        // Assert
        assertDoesNotThrow(() -> UUID.fromString(traceId));
        assertEquals(traceId, response.getHeader(TraceIdFilter.CABECERA));
    }

    @Test
    @DisplayName("Ignora una cabecera que no es un UUID y genera un traceId nuevo")
    void ignoresInvalidHeader() throws Exception {
        // Arrange
        request.addHeader(TraceIdFilter.CABECERA, "<script>alert(1)</script>");

        // Act
        String traceId = traceIdSeenByChain();

        // Assert
        assertNotEquals("<script>alert(1)</script>", traceId);
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }

    @Test
    @DisplayName("Quita el traceId del MDC al terminar la petición")
    void removesTraceIdFromMdcAfterRequest() throws Exception {
        // Act
        traceIdSeenByChain();

        // Assert
        assertNull(MDC.get(TraceIdFilter.CLAVE_MDC));
    }

    @Test
    @DisplayName("Quita el traceId del MDC aunque la cadena de filtros falle")
    void removesTraceIdFromMdcWhenChainFails() throws Exception {
        // Arrange
        FilterChain chain = mock(FilterChain.class);
        doThrow(new IllegalStateException("fallo")).when(chain).doFilter(any(), any());

        // Act
        assertThrows(IllegalStateException.class, () -> filter.doFilterInternal(request, response, chain));

        // Assert
        assertNull(MDC.get(TraceIdFilter.CLAVE_MDC));
    }

    @Test
    @DisplayName("traceIdActual devuelve el traceId de la petición en curso")
    void currentTraceIdReturnsMdcValue() {
        // Arrange
        MDC.put(TraceIdFilter.CLAVE_MDC, "traza-en-curso");

        // Act & Assert
        assertEquals("traza-en-curso", TraceIdFilter.traceIdActual());
    }

    @Test
    @DisplayName("traceIdActual genera uno nuevo cuando no hay petición en curso")
    void currentTraceIdGeneratesOneWithoutRequest() {
        // Act
        String traceId = TraceIdFilter.traceIdActual();

        // Assert
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }
}
