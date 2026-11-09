package com.service_reservation.EAP03.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Escribe el error estándar {errorCode, message, traceId} directamente en la respuesta.
 * Es para los errores que ocurren en los filtros, antes de llegar a los controladores,
 * donde los @ExceptionHandler no actúan.
 */
public final class RespuestaErrorJson {

    public static final String ACCESO_NO_AUTORIZADO = "ACCESO_NO_AUTORIZADO";
    public static final String MENSAJE_ACCESO_NO_AUTORIZADO = "No tiene privilegios para realizar esta operación";

    private RespuestaErrorJson() {
    }

    public static void escribir(HttpServletResponse response, HttpStatus estado, String errorCode, String mensaje)
            throws IOException {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("errorCode", errorCode);
        cuerpo.put("message", mensaje);
        cuerpo.put("traceId", TraceIdFilter.traceIdActual());

        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(JsonMapper.shared().writeValueAsString(cuerpo));
    }
}
