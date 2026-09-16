package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioSpringDataRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    boolean existsByEmail(String email);
}