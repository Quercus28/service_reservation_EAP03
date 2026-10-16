package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteSpringDataRepository extends JpaRepository<ClienteJpaEntity, Long> {

    Optional<ClienteJpaEntity> findByIdUsuario(Long idUsuario);
}