package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.JwtAuthenticationFilter;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize / @PostAuthorize en toda la aplicación
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectProvider<RegistrarAccesoDenegadoUseCase> registrarAccesoDenegadoUseCaseProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.registrarAccesoDenegadoUseCaseProvider = registrarAccesoDenegadoUseCaseProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (API REST sin estado)
            .csrf(csrf -> csrf.disable())

            // Sin sesión HTTP — cada request debe autenticarse con JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/registro").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/2fa/verificar-login").permitAll()
                .requestMatchers("/api/v1/auth/2fa/qr").authenticated()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/v1/agendas/**").hasRole("PROVEEDOR")
                .anyRequest().authenticated()
            )

            // Insertar el filtro JWT antes del filtro estándar de usuario/contraseña
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 401 — Sin autenticación válida
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"No autenticado\"," +
                        "\"message\":\"Se requiere autenticación para acceder a este recurso.\",\"path\":\"%s\"}",
                        Instant.now(), request.getRequestURI()
                    ));
                })
                // 403 — Autenticado pero sin el rol necesario
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(String.format(
                        "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Acceso denegado\"," +
                        "\"message\":\"No tiene los permisos necesarios para realizar esta acción.\",\"path\":\"%s\"}",
                        Instant.now(), request.getRequestURI()
                    ));

                    // Auditoría de intentos de acceso denegado a rutas administrativas
                    if (request.getRequestURI().startsWith("/api/admin")) {
                        RegistrarAccesoDenegadoUseCase auditUseCase = registrarAccesoDenegadoUseCaseProvider.getIfAvailable();
                        if (auditUseCase != null) {
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            Long idUsuario = null;
                            String email = null;
                            if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
                                idUsuario = principal.getIdUsuario();
                                email = principal.getEmail();
                            } else if (auth != null) {
                                email = auth.getName();
                            }
                            auditUseCase.registrar(new IntentoAccesoDenegado(
                                    LocalDateTime.now(),
                                    idUsuario,
                                    email,
                                    request.getRemoteAddr(),
                                    request.getMethod(),
                                    request.getRequestURI()
                            ));
                        }
                    }
                })
            )

            // Permitir frames de H2-console (solo desarrollo)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}