package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.BloqueoRecursoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BloqueoRecursoMapper {

    public BloqueoRecursoJpaEntity aEntidad(BloqueoRecurso bloqueo) {
        BloqueoRecursoJpaEntity entidad = new BloqueoRecursoJpaEntity();
        entidad.setId(bloqueo.getId());
        entidad.setIdRecurso(bloqueo.getIdRecurso());
        entidad.setFechaInicio(bloqueo.getFechaInicio());
        entidad.setFechaFin(bloqueo.getFechaFin());
        entidad.setMotivo(bloqueo.getMotivo());
        entidad.setCreatedAt(bloqueo.getCreatedAt());
        return entidad;
    }

    public BloqueoRecurso aDominio(BloqueoRecursoJpaEntity entidad) {
        return BloqueoRecurso.reconstruir(
                entidad.getId(),
                entidad.getIdRecurso(),
                entidad.getFechaInicio(),
                entidad.getFechaFin(),
                entidad.getMotivo(),
                entidad.getCreatedAt()
        );
    }
}
