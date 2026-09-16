package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.out.BloqueoFuerzaBrutaPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryBloqueoFuerzaBrutaAdapter implements BloqueoFuerzaBrutaPort {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final Map<String, RegistroIntento> intentosMap = new ConcurrentHashMap<>();

    private record RegistroIntento(int intentos, Instant bloqueadoHasta) {}

    @Override
    public boolean estaBloqueado(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return false;
        }

        RegistroIntento registro = intentosMap.get(identificador);
        if (registro == null) {
            return false;
        }

        if (registro.bloqueadoHasta() != null) {
            if (Instant.now().isBefore(registro.bloqueadoHasta())) {
                return true;
            } else {
                // El bloqueo de 15 minutos ya expiró: limpiar el registro
                intentosMap.remove(identificador);
                return false;
            }
        }

        return false;
    }

    @Override
    public void registrarIntentoFallido(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return;
        }

        intentosMap.compute(identificador, (key, registroActual) -> {
            Instant ahora = Instant.now();

            if (registroActual == null || (registroActual.bloqueadoHasta() != null && ahora.isAfter(registroActual.bloqueadoHasta()))) {
                // Primer intento o intento posterior a un bloqueo ya expirado
                return new RegistroIntento(1, null);
            }

            int nuevosIntentos = registroActual.intentos() + 1;
            Instant nuevoBloqueo = nuevosIntentos >= MAX_INTENTOS_FALLIDOS ? ahora.plus(DURACION_BLOQUEO) : null;

            return new RegistroIntento(nuevosIntentos, nuevoBloqueo);
        });
    }

    @Override
    public void resetearIntentos(String identificador) {
        if (identificador != null) {
            intentosMap.remove(identificador);
        }
    }
}
