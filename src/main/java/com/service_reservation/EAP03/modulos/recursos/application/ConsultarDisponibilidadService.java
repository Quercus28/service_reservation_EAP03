package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;
    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;
    private final BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;

    public ConsultarDisponibilidadService(RecursoRepositoryPort recursoRepositoryPort,
                                          RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort,
                                          HorarioRecursoRepositoryPort horarioRecursoRepositoryPort,
                                          BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
        this.bloqueoRecursoRepositoryPort = bloqueoRecursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadRecurso ejecutar(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new DatosInvalidosException("El rango de fechas es invalido: 'hasta' debe ser posterior a 'desde'");
        }

        Recurso recurso = recursoRepositoryPort.buscarPorId(idRecurso)
                .orElseThrow(() -> new RecursoNoEncontradoException(idRecurso));

        List<RecursoPrestado> posiblesChoques =
                recursoPrestadoRepositoryPort.buscarQueSeSolapan(idRecurso, desde, hasta);

        int comprometido = 0;
        for (RecursoPrestado prestamo : posiblesChoques) {
            if (prestamo.seSolapaCon(desde, hasta)) {
                comprometido += prestamo.getCantidad();
            }
        }

        boolean operativo = esOperativo(idRecurso, desde, hasta);
        int disponible = operativo ? recurso.unidadesDisponibles(comprometido) : 0;

        return new DisponibilidadRecurso(
                idRecurso,
                desde,
                hasta,
                recurso.getStock(),
                comprometido,
                disponible,
                operativo
        );
    }

    // Regla de compatibilidad (HU06): un recurso SIN ningún horario configurado se considera
    // operativo a cualquier hora, que es como funcionaban todos los recursos antes de esta
    // HU; así los recursos existentes no quedan sin disponibilidad al desplegar. En cuanto el
    // proveedor registra al menos una franja, el rango tiene que caber completo en alguna.
    // Los bloqueos aplican siempre, tenga o no horarios el recurso.
    private boolean esOperativo(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        List<HorarioRecurso> horarios = horarioRecursoRepositoryPort.buscarPorRecurso(idRecurso);
        if (!horarios.isEmpty() && horarios.stream().noneMatch(h -> h.contiene(desde, hasta))) {
            return false;
        }
        return bloqueoRecursoRepositoryPort.buscarQueSeSolapan(idRecurso, desde, hasta).stream()
                .noneMatch(b -> b.seSolapaCon(desde, hasta));
    }
}
