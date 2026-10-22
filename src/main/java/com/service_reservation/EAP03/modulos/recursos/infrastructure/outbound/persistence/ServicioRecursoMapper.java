package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.ServicioRecursoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ServicioRecursoMapper {

    public ServicioRecursoJpaEntity aEntidad(ServicioRecurso servicioRecurso) {
        ServicioRecursoJpaEntity entidad = new ServicioRecursoJpaEntity();
        entidad.setId(servicioRecurso.getId());
        entidad.setIdServicio(servicioRecurso.getIdServicio());
        entidad.setIdRecurso(servicioRecurso.getIdRecurso());
        entidad.setCantidad(servicioRecurso.getCantidad());
        entidad.setObligatorio(servicioRecurso.isObligatorio());
        return entidad;
    }

    public ServicioRecurso aDominio(ServicioRecursoJpaEntity entidad) {
        return ServicioRecurso.reconstruir(
                entidad.getId(),
                entidad.getIdServicio(),
                entidad.getIdRecurso(),
                entidad.getCantidad(),
                entidad.getObligatorio()
        );
    }
}
