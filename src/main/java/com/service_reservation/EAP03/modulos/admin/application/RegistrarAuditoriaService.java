package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class RegistrarAuditoriaService implements RegistrarAuditoriaUseCase {

    public static final int DETALLE_MAX = 255;
    public static final int ENDPOINT_MAX = 255;
    static final String OMITIDO = "[omitido]";

    // Por si alguien manda por error un dato personal en el texto: correos y números largos
    // (7 o más dígitos, como un teléfono o un documento) se reemplazan antes de guardar.
    private static final Pattern CORREO = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private static final Pattern NUMERO_LARGO = Pattern.compile("\\+?\\d(?:[\\s-]?\\d){6,}");

    private final AuditoriaPort auditoriaPort;
    private final Clock clock;

    public RegistrarAuditoriaService(AuditoriaPort auditoriaPort, Clock clock) {
        this.auditoriaPort = auditoriaPort;
        this.clock = clock;
    }

    // Va en su propia transacción: si quien llama deshace la suya (por ejemplo, porque el
    // cambio fue rechazado), el registro de auditoría igual queda guardado.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ejecutar(EventoAuditoria evento) {
        Objects.requireNonNull(evento, "evento es obligatorio");
        Objects.requireNonNull(evento.accion(), "accion es obligatoria");
        Objects.requireNonNull(evento.resultado(), "resultado es obligatorio");
        auditoriaPort.registrar(new RegistroAuditoria(
                LocalDateTime.now(clock),
                evento.idActor(),
                evento.idUsuarioAfectado(),
                evento.accion(),
                null,
                evento.resultado(),
                limpiar(sinParametros(evento.endpoint()), ENDPOINT_MAX),
                limpiar(evento.detalle(), DETALLE_MAX)
        ));
    }

    // Lo que va después de "?" en la ruta puede traer valores enviados por el usuario.
    private static String sinParametros(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        int corte = endpoint.indexOf('?');
        return corte >= 0 ? endpoint.substring(0, corte) : endpoint;
    }

    private static String limpiar(String texto, int maximo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = CORREO.matcher(texto.trim()).replaceAll(OMITIDO);
        limpio = NUMERO_LARGO.matcher(limpio).replaceAll(OMITIDO);
        return limpio.length() > maximo ? limpio.substring(0, maximo) : limpio;
    }
}
