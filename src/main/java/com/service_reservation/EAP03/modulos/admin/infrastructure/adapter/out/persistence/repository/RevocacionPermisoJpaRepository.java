package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository;

import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RevocacionPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevocacionPermisoJpaRepository extends JpaRepository<RevocacionPermisoEntity, Long> {

    Optional<RevocacionPermisoEntity> findByIdUsuarioAndRolAndEstado(Long idUsuario, String rol, String estado);

    List<RevocacionPermisoEntity> findByEstado(String estado);
}
