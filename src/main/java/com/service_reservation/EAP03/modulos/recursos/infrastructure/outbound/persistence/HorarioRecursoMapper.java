package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.HorarioRecursoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class HorarioRecursoMapper {

    public HorarioRecursoJpaEntity aEntidad(HorarioRecurso horario) {
        HorarioRecursoJpaEntity entidad = new HorarioRecursoJpaEntity();
        entidad.setId(horario.getId());
        entidad.setIdRecurso(horario.getIdRecurso());
        entidad.setDiaSemana(horario.getDiaSemana());
        entidad.setHoraInicio(horario.getHoraInicio());
        entidad.setHoraFin(horario.getHoraFin());
        return entidad;
    }

    public HorarioRecurso aDominio(HorarioRecursoJpaEntity entidad) {
        return HorarioRecurso.reconstruir(
                entidad.getId(),
                entidad.getIdRecurso(),
                entidad.getDiaSemana(),
                entidad.getHoraInicio(),
                entidad.getHoraFin()
        );
    }
}
