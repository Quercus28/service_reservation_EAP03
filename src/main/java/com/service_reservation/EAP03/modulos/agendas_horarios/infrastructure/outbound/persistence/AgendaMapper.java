package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.HorarioJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class AgendaMapper {
    public Agenda toDomain(AgendaJpaEntity entity) {
        return new Agenda(
                entity.getId(),
                entity.getIdProveedor(),
                entity.getIdServicio(),
                entity.isActiva(),
                entity.getHorarios().stream()
                        .map(h -> new Horario(h.getId(), h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin()))
                        .toList()
        );
    }

    public AgendaJpaEntity toEntity(Agenda domain) {
        AgendaJpaEntity entity = new AgendaJpaEntity();
        entity.setId(domain.getId());
        entity.setIdProveedor(domain.getProveedorId());
        entity.setIdServicio(domain.getServicioId());
        entity.setActiva(domain.isActiva());

        var horarios = new ArrayList<HorarioJpaEntity>();
        for (Horario horario : domain.getHorarios()) {
            HorarioJpaEntity h = new HorarioJpaEntity();
            h.setId(horario.id());
            h.setAgenda(entity);
            h.setDiaSemana(horario.diaSemana());
            h.setHoraInicio(horario.horaInicio());
            h.setHoraFin(horario.horaFin());
            horarios.add(h);
        }
        entity.setHorarios(horarios);
        return entity;
    }
}