package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RevocacionPermisoEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RevocacionPermisoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RevocacionPermisoPersistenceAdapter implements RevocacionPermisoRepositoryPort {

    private final RevocacionPermisoJpaRepository repository;

    public RevocacionPermisoPersistenceAdapter(RevocacionPermisoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RevocacionPermiso guardar(RevocacionPermiso revocacion) {
        RevocacionPermisoEntity entity = mapToEntity(revocacion);
        RevocacionPermisoEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<RevocacionPermiso> buscarPendiente(Long idUsuario, RolOperativo rol) {
        return repository.findByIdUsuarioAndRolAndEstado(
                idUsuario, rol.name(), EstadoRevocacion.PENDIENTE_CANCELACION.name())
                .map(this::mapToDomain);
    }

    @Override
    public List<RevocacionPermiso> listar(EstadoRevocacion estado) {
        List<RevocacionPermisoEntity> entities = estado == null
                ? repository.findAll()
                : repository.findByEstado(estado.name());
        return entities.stream().map(this::mapToDomain).toList();
    }

    private RevocacionPermiso mapToDomain(RevocacionPermisoEntity entity) {
        return new RevocacionPermiso(
                entity.getId(),
                entity.getIdUsuario(),
                RolOperativo.valueOf(entity.getRol()),
                EstadoRevocacion.valueOf(entity.getEstado()),
                entity.getMotivo(),
                entity.getIdAdmin(),
                entity.getFechaSolicitud(),
                entity.getFechaLimite(),
                entity.getFechaCierre(),
                entity.getReservasCanceladas()
        );
    }

    private RevocacionPermisoEntity mapToEntity(RevocacionPermiso domain) {
        RevocacionPermisoEntity entity = new RevocacionPermisoEntity();
        entity.setId(domain.getId());
        entity.setIdUsuario(domain.getIdUsuario());
        entity.setRol(domain.getRol().name());
        entity.setEstado(domain.getEstado().name());
        entity.setMotivo(domain.getMotivo());
        entity.setIdAdmin(domain.getIdAdmin());
        entity.setFechaSolicitud(domain.getFechaSolicitud());
        entity.setFechaLimite(domain.getFechaLimite());
        entity.setFechaCierre(domain.getFechaCierre());
        entity.setReservasCanceladas(domain.getReservasCanceladas());
        return entity;
    }
}
