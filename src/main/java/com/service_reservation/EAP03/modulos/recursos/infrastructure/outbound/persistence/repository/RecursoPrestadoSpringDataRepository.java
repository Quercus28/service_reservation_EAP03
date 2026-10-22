package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoPrestadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RecursoPrestadoSpringDataRepository extends JpaRepository<RecursoPrestadoJpaEntity, Integer> {

    // Los préstamos liberados (reserva cancelada) ya no ocupan unidades.
    @Query("SELECT p FROM RecursoPrestadoJpaEntity p WHERE p.idRecurso = :idRecurso AND p.liberadoEn IS NULL AND p.fechaInicio < :hasta AND :desde < p.fechaFin")
    List<RecursoPrestadoJpaEntity> buscarQueSeSolapan(@Param("idRecurso") Integer idRecurso,
                                                      @Param("desde") LocalDateTime desde,
                                                      @Param("hasta") LocalDateTime hasta);

    @Query("SELECT p FROM RecursoPrestadoJpaEntity p WHERE p.idRecurso IN :idsRecurso AND p.liberadoEn IS NULL AND p.fechaInicio < :hasta AND :desde < p.fechaFin")
    List<RecursoPrestadoJpaEntity> buscarQueSeSolapanDeRecursos(@Param("idsRecurso") Collection<Integer> idsRecurso,
                                                                @Param("desde") LocalDateTime desde,
                                                                @Param("hasta") LocalDateTime hasta);

    List<RecursoPrestadoJpaEntity> findByIdServicioPrestadoAndLiberadoEnIsNull(Integer idServicioPrestado);
}
