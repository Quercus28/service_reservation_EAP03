package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioRolJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRolSpringDataRepository extends JpaRepository<UsuarioRolJpaEntity, UsuarioRolJpaEntity.Id> {
}
