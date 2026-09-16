package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.RolJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolSpringDataRepository extends JpaRepository<RolJpaEntity, Long> {
    Optional<RolJpaEntity> findByNombre(String nombre);
}