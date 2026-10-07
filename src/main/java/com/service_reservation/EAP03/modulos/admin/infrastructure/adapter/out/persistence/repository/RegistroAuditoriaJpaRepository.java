package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository;

import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RegistroAuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroAuditoriaJpaRepository extends JpaRepository<RegistroAuditoriaEntity, Long> {
}
