package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteSpringDataRepository extends JpaRepository<ClienteJpaEntity, Long> {}