package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta cada request HTTP. Si el header Authorization contiene
 * un Bearer token JWT valido, construye la autenticacion y la deposita en el SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String RUTA_QR_2FA = "/api/v1/auth/2fa/qr";
    private static final String RUTA_ACTIVAR_2FA = "/api/v1/auth/2fa/activar";
    // No empieza por ROLE_, así que ningún rol guardado en un JWT puede hacerse pasar por este permiso.
    public static final String PERMISO_CONFIGURAR_2FA = "ETAPA_CONFIGURAR_2FA";

    private final JwtPort jwtPort;

    public JwtAuthenticationFilter(JwtPort jwtPort) {
        this.jwtPort = jwtPort;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/v3/api-docs.yaml");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtPort.validarToken(token)) {
            log.debug("Token JWT rechazado para [{}] {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Un token temporal (de cualquier etapa) nunca es una sesión normal. La única excepción es
        // el token de configuración de 2FA, y solo en las dos rutas donde se configura el 2FA.
        String etapa = jwtPort.extraerEtapa(token);
        if (etapa != null) {
            if (JwtPort.ETAPA_CONFIGURAR_2FA.equals(etapa) && esRutaDeConfiguracion2fa(request)) {
                autenticarConfiguracion2fa(token);
            } else {
                log.debug("Token temporal 2FA ignorado como sesión para [{}] {}", request.getMethod(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtPort.extraerEmail(token);
            Long idUsuario = jwtPort.extraerUsuarioId(token);
            List<String> roles = jwtPort.extraerRoles(token);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(rol -> rol.startsWith("ROLE_") ? rol : "ROLE_" + rol)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsuarioAutenticadoPrincipal principal =
                    new UsuarioAutenticadoPrincipal(idUsuario, email, roles);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Autenticacion establecida para usuario '{}' con roles {}", email, roles);

        } catch (Exception e) {
            log.warn("No se pudo establecer autenticacion desde token JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // Se compara la ruta sin el contexto de la aplicación y con el método exacto.
    private static boolean esRutaDeConfiguracion2fa(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        String metodo = request.getMethod();
        return ("GET".equals(metodo) && RUTA_QR_2FA.equals(ruta))
                || ("POST".equals(metodo) && RUTA_ACTIVAR_2FA.equals(ruta));
    }

    // Sin roles: el único permiso es PERMISO_CONFIGURAR_2FA, que solo acepta POST /2fa/activar.
    private void autenticarConfiguracion2fa(String token) {
        try {
            UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.enConfiguracion2fa(
                    jwtPort.extraerUsuarioId(token), jwtPort.extraerEmail(token));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority(PERMISO_CONFIGURAR_2FA))));
        } catch (Exception e) {
            log.warn("No se pudo leer el token de configuración 2FA: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}