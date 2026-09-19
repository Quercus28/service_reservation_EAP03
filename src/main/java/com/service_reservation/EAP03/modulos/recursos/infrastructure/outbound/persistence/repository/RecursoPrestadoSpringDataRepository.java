package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoPrestadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecursoPrestadoSpringDataRepository extends JpaRepository<RecursoPrestadoJpaEntity, Integer> {

    @Query("SELECT p FROM RecursoPrestadoJpaEntity p WHERE p.idRecurso = :idRecurso AND p.fechaInicio < :hasta AND :desde < p.fechaFin")
    List<RecursoPrestadoJpaEntity> buscarQueSeSolapan(@Param("idRecurso") Integer idRecurso,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta);
}
