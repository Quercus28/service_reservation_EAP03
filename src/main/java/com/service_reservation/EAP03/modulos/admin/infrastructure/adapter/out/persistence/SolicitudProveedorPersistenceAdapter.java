package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.SolicitudProveedorRepositoryPort;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.SolicitudAprobacionProveedorEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.SolicitudAprobacionProveedorJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SolicitudProveedorPersistenceAdapter implements SolicitudProveedorRepositoryPort {

    private final SolicitudAprobacionProveedorJpaRepository repository;

    public SolicitudProveedorPersistenceAdapter(SolicitudAprobacionProveedorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SolicitudAprobacionProveedor guardar(SolicitudAprobacionProveedor solicitud) {
        SolicitudAprobacionProveedorEntity entity = mapToEntity(solicitud);
        SolicitudAprobacionProveedorEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<SolicitudAprobacionProveedor> buscarPorId(Long id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<SolicitudAprobacionProveedor> buscarUltimaPorUsuario(Long idUsuario) {
        return repository.findTopByIdUsuarioOrderByFechaSolicitudDesc(idUsuario)
                .map(this::mapToDomain);
    }

    @Override
    public boolean existePendienteParaUsuario(Long idUsuario) {
        return repository.existsByIdUsuarioAndEstado(idUsuario, EstadoSolicitud.PENDIENTE.name());
    }

    @Override
    public boolean existeAprobadaParaUsuario(Long idUsuario) {
        return repository.existsByIdUsuarioAndEstado(idUsuario, EstadoSolicitud.APROBADA.name());
    }

    @Override
    public List<SolicitudAprobacionProveedor> listar(EstadoSolicitud estado) {
        List<SolicitudAprobacionProveedorEntity> entities = estado == null
                ? repository.findAll()
                : repository.findByEstado(estado.name());
        return entities.stream().map(this::mapToDomain).toList();
    }

    private SolicitudAprobacionProveedor mapToDomain(SolicitudAprobacionProveedorEntity entity) {
        return new SolicitudAprobacionProveedor(
                entity.getId(),
                entity.getIdUsuario(),
                EstadoSolicitud.valueOf(entity.getEstado()),
                entity.getFechaSolicitud(),
                entity.getFechaResolucion(),
                entity.getIdAdminResolutor(),
                entity.getMotivoRechazo()
        );
    }

    private SolicitudAprobacionProveedorEntity mapToEntity(SolicitudAprobacionProveedor domain) {
        SolicitudAprobacionProveedorEntity entity = new SolicitudAprobacionProveedorEntity();
        entity.setId(domain.getId());
        entity.setIdUsuario(domain.getIdUsuario());
        entity.setEstado(domain.getEstado().name());
        entity.setFechaSolicitud(domain.getFechaSolicitud());
        entity.setFechaResolucion(domain.getFechaResolucion());
        entity.setIdAdminResolutor(domain.getIdAdminResolutor());
        entity.setMotivoRechazo(domain.getMotivoRechazo());
        return entity;
    }
}
