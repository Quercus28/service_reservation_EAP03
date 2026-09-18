package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecursoSpringDataRepository extends JpaRepository<RecursoJpaEntity, Integer> {

    Page<RecursoJpaEntity> findByIdProveedorAndEliminadoEnIsNull(Integer idProveedor, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecursoJpaEntity r WHERE r.id = :id")
    Optional<RecursoJpaEntity> buscarPorIdConBloqueo(@Param("id") Integer id);

    @Query("SELECT COUNT(r) > 0 FROM RecursoJpaEntity r WHERE r.idProveedor = :idProveedor AND LOWER(r.nombre) = LOWER(:nombre) AND r.eliminadoEn IS NULL AND (:idExcluir IS NULL OR r.id <> :idExcluir)")
    boolean existeNombreActivo(@Param("idProveedor") Integer idProveedor, @Param("nombre") String nombre, @Param("idExcluir") Integer idExcluir);
}
