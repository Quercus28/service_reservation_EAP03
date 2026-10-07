package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository;

import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.SolicitudAprobacionProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAprobacionProveedorJpaRepository extends JpaRepository<SolicitudAprobacionProveedorEntity, Long> {

    Optional<SolicitudAprobacionProveedorEntity> findTopByIdUsuarioOrderByFechaSolicitudDesc(Long idUsuario);

    boolean existsByIdUsuarioAndEstado(Long idUsuario, String estado);

    List<SolicitudAprobacionProveedorEntity> findByEstado(String estado);
}
