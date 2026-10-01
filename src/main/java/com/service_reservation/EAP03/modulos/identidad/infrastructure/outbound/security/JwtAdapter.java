package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.model.Usuario;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.JwtPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtAdapter implements JwtPort {

    private static final Logger log = LoggerFactory.getLogger(JwtAdapter.class);

    private final SecretKey signingKey;
    private final long expirationInSeconds;

    public JwtAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-in-seconds:3600}") long expirationInSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationInSeconds = expirationInSeconds;
    }

    // -------------------------------------------------------------------------
    // Generación
    // -------------------------------------------------------------------------

    @Override
    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationInSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("roles", new ArrayList<>(usuario.getRoles()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public long getExpiracionSegundos() {
        return expirationInSeconds;
    }

    @Override
    public String generarTokenTemporal2fa(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiry = now.plus(5, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("stage", "2FA_PENDING")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean esTokenTemporal2faValido(String token) {
        try {
            Claims claims = parsearClaims(token);
            return "2FA_PENDING".equals(claims.get("stage"));
        } catch (Exception e) {
            log.warn("Token temporal 2FA inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Validación y extracción
    // -------------------------------------------------------------------------

    @Override
    public boolean validarToken(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Error al validar token JWT: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public String extraerEmail(String token) {
        return parsearClaims(token).getSubject();
    }

    @Override
    public Long extraerUsuarioId(String token) {
        Object idClaim = parsearClaims(token).get("id");
        if (idClaim instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        Object rolesClaim = parsearClaims(token).get("roles");
        if (rolesClaim instanceof List<?> lista) {
            return (List<String>) lista;
        }
        return Collections.emptyList();
    }

    // -------------------------------------------------------------------------
    // Helpers internos
    // -------------------------------------------------------------------------

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
