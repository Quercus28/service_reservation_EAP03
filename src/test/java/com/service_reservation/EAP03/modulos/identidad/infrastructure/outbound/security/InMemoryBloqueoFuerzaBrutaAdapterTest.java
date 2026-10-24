package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBloqueoFuerzaBrutaAdapterTest {

    private static final String EMAIL = "ana@test.com";

    private final InMemoryBloqueoFuerzaBrutaAdapter adapter = new InMemoryBloqueoFuerzaBrutaAdapter();

    private void failAttempts(int veces) {
        for (int i = 0; i < veces; i++) {
            adapter.registrarIntentoFallido(EMAIL);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> attempts() throws Exception {
        Field campo = InMemoryBloqueoFuerzaBrutaAdapter.class.getDeclaredField("intentosMap");
        campo.setAccessible(true);
        return (Map<String, Object>) campo.get(adapter);
    }

    private void forceRecord(int intentos, Instant bloqueadoHasta) throws Exception {
        Class<?> tipoRegistro = Class.forName(InMemoryBloqueoFuerzaBrutaAdapter.class.getName() + "$RegistroIntento");
        Constructor<?> constructor = tipoRegistro.getDeclaredConstructor(int.class, Instant.class);
        constructor.setAccessible(true);
        attempts().put(EMAIL, constructor.newInstance(intentos, bloqueadoHasta));
    }

    @Test
    @DisplayName("Un identificador sin intentos fallidos no está bloqueado")
    void notLockedWithoutAttempts() {
        assertFalse(adapter.estaBloqueado(EMAIL));
    }

    @Test
    @DisplayName("Cuatro intentos fallidos no bloquean la cuenta")
    void fourAttemptsDoNotLock() {
        // Act
        failAttempts(4);

        // Assert
        assertFalse(adapter.estaBloqueado(EMAIL));
    }

    @Test
    @DisplayName("El quinto intento fallido bloquea la cuenta")
    void fifthAttemptLocks() {
        // Act
        failAttempts(5);

        // Assert
        assertTrue(adapter.estaBloqueado(EMAIL));
        assertFalse(adapter.estaBloqueado("otro@test.com"));
    }

    @Test
    @DisplayName("Un intento fallido durante el bloqueo lo mantiene activo")
    void attemptDuringLockKeepsIt() {
        // Act
        failAttempts(6);

        // Assert
        assertTrue(adapter.estaBloqueado(EMAIL));
    }

    @Test
    @DisplayName("Resetear los intentos desbloquea la cuenta")
    void resetUnlocks() {
        // Arrange
        failAttempts(5);

        // Act
        adapter.resetearIntentos(EMAIL);

        // Assert
        assertFalse(adapter.estaBloqueado(EMAIL));
    }

    @Test
    @DisplayName("Resetear con identificador nulo no hace nada")
    void resetWithNullDoesNothing() {
        // Arrange
        failAttempts(5);

        // Act
        adapter.resetearIntentos(null);

        // Assert
        assertTrue(adapter.estaBloqueado(EMAIL));
    }

    @Test
    @DisplayName("Un bloqueo expirado deja de bloquear y se elimina el registro")
    void expiredLockIsCleared() throws Exception {
        // Arrange
        forceRecord(5, Instant.now().minusSeconds(1));

        // Act
        boolean bloqueado = adapter.estaBloqueado(EMAIL);

        // Assert
        assertFalse(bloqueado);
        assertFalse(attempts().containsKey(EMAIL));
    }

    @Test
    @DisplayName("Tras un bloqueo expirado el conteo de intentos empieza de nuevo")
    void attemptAfterExpiredLockRestartsCount() throws Exception {
        // Arrange
        forceRecord(5, Instant.now().minusSeconds(1));

        // Act
        failAttempts(4);

        // Assert
        assertFalse(adapter.estaBloqueado(EMAIL));
        failAttempts(1);
        assertTrue(adapter.estaBloqueado(EMAIL));
    }

    @ParameterizedTest(name = "identificador = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Identificadores nulos o en blanco nunca se registran ni se bloquean")
    void nullOrBlankIdentifier(String identificador) throws Exception {
        // Act
        for (int i = 0; i < 5; i++) {
            adapter.registrarIntentoFallido(identificador);
        }

        // Assert
        assertFalse(adapter.estaBloqueado(identificador));
        assertTrue(attempts().isEmpty());
    }
}
