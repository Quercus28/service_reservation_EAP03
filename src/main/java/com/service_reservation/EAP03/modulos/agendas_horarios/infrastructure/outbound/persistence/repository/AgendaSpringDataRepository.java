package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgendaSpringDataRepository extends JpaRepository<AgendaJpaEntity, Long> {
    Optional<AgendaJpaEntity> findByIdAndIdProveedorAndActivaTrue(Long id, Integer idProveedor);
    Optional<AgendaJpaEntity> findByIdServicioAndActivaTrue(Integer idServicio);
    boolean existsByIdServicioAndActivaTrue(Integer idServicio);
}
