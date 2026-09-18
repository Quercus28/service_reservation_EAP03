package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ProveedorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProveedorSpringDataRepository extends JpaRepository<ProveedorJpaEntity, Long> {

    Optional<ProveedorJpaEntity> findByIdUsuario(Long idUsuario);
}
