package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import org.springframework.stereotype.Component;

@Component
public class ServicioMapper {

    public Servicio toDomain(ServicioJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Servicio.reconstituir(
                entity.getId(),
                entity.getIdProveedor(),
                entity.getNombre(),
                entity.getDuracionMinutos(),
                entity.getCreatedAt()
        );
    }

    public ServicioJpaEntity toJpaEntity(Servicio domain) {
        if (domain == null) {
            return null;
        }
        return new ServicioJpaEntity(
                domain.getId(),
                domain.getIdProveedor(),
                domain.getDuracionMinutos(),
                domain.getNombre(),
                domain.getCreatedAt()
        );
    }
}
