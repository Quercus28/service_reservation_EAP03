package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioSpringDataRepository extends JpaRepository<UsuarioJpaEntity, Long> {
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "roles")
    Optional<UsuarioJpaEntity> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UsuarioJpaEntity> findById(Long id);
}