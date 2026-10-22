package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.AgendaDeRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        AgendaDeRecurso agenda = new AgendaDeRecurso(
                recurso,
                horarioRecursoRepositoryPort.buscarPorRecurso(idRecurso),
                bloqueoRecursoRepositoryPort.buscarQueSeSolapan(idRecurso, desde, hasta),
                recursoPrestadoRepositoryPort.buscarQueSeSolapan(idRecurso, desde, hasta)
        );

        int comprometido = agenda.comprometido(desde, hasta);
        boolean operativo = agenda.esOperativo(desde, hasta);
        int disponible = agenda.disponible(desde, hasta);

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
}
