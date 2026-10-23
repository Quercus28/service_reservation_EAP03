package com.service_reservation.EAP03.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Le da a cada petición un traceId: lo deja en el MDC (para que salga en los logs)
 * y lo devuelve en la cabecera X-Trace-Id. Corre antes que cualquier otro filtro.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Trace-Id";
    public static final String CLAVE_MDC = "traceId";

    private static final Pattern UUID_VALIDO =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = traceIdDe(request.getHeader(CABECERA));
        MDC.put(CLAVE_MDC, traceId);
        // Se pone antes de seguir para que salga aunque la respuesta se escriba más adelante.
        response.setHeader(CABECERA, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CLAVE_MDC);
        }
    }

    /**
     * El traceId de la petición en curso. Si no hay (por ejemplo en un test sin el filtro),
     * se genera uno para que la respuesta de error nunca quede sin traceId.
     */
    public static String traceIdActual() {
        String traceId = MDC.get(CLAVE_MDC);
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    // Solo se acepta el traceId de la cabecera si es un UUID válido, para no meter texto
    // arbitrario del cliente en los logs ni en las respuestas.
    private static String traceIdDe(String cabecera) {
        if (cabecera != null && UUID_VALIDO.matcher(cabecera).matches()) {
            return cabecera.toLowerCase();
        }
        return UUID.randomUUID().toString();
    }
}
