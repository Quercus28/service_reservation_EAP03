package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RegistroAuditoriaEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RegistroAuditoriaJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaPersistenceAdapter implements AuditoriaPort {

    private final RegistroAuditoriaJpaRepository repository;

    public AuditoriaPersistenceAdapter(RegistroAuditoriaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void registrar(RegistroAuditoria registro) {
        RegistroAuditoriaEntity entity = new RegistroAuditoriaEntity();
        entity.setFecha(registro.fecha());
        entity.setIdActor(registro.idActor());
        entity.setIdUsuarioAfectado(registro.idUsuarioAfectado());
        entity.setAccion(registro.accion().name());
        entity.setRol(registro.rol() != null ? registro.rol().name() : null);
        entity.setResultado(registro.resultado().name());
        entity.setEndpoint(registro.endpoint());
        entity.setDetalle(registro.detalle());
        
        repository.save(entity);
    }
}
