package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.BloqueoRecursoJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BloqueoRecursoSpringDataRepository extends JpaRepository<BloqueoRecursoJpaEntity, Integer> {

    @Query("SELECT b FROM BloqueoRecursoJpaEntity b WHERE b.idRecurso = :idRecurso AND b.fechaInicio < :hasta AND :desde < b.fechaFin")
    List<BloqueoRecursoJpaEntity> buscarQueSeSolapan(@Param("idRecurso") Integer idRecurso,
                                                     @Param("desde") LocalDateTime desde,
                                                     @Param("hasta") LocalDateTime hasta);

    Page<BloqueoRecursoJpaEntity> findByIdRecursoOrderByFechaInicioAsc(Integer idRecurso, Pageable pageable);
}
