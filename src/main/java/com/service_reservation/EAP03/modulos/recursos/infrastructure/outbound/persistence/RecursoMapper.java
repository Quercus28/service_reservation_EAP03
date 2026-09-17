package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecursoMapper {

    public RecursoJpaEntity aEntidad(Recurso recurso) {
        RecursoJpaEntity entidad = new RecursoJpaEntity();
        entidad.setId(recurso.getId());
        entidad.setIdProveedor(recurso.getIdProveedor());
        entidad.setNombre(recurso.getNombre());
        entidad.setPrecioUnitario(recurso.getPrecioUnitario());
        entidad.setStock(recurso.getStock());
        entidad.setCreatedAt(recurso.getCreatedAt());
        entidad.setEliminadoEn(recurso.getEliminadoEn());
        return entidad;
    }

    public Recurso aDominio(RecursoJpaEntity entidad) {
        return Recurso.reconstruir(
                entidad.getId(),
                entidad.getIdProveedor(),
                entidad.getNombre(),
                entidad.getPrecioUnitario(),
                entidad.getStock(),
                entidad.getCreatedAt(),
                entidad.getEliminadoEn()
        );
    }
}
