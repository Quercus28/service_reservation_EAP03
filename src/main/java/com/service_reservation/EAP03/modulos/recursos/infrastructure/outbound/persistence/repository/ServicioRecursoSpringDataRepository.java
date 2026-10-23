package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.ServicioRecursoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicioRecursoSpringDataRepository extends JpaRepository<ServicioRecursoJpaEntity, Integer> {

    List<ServicioRecursoJpaEntity> findByIdServicioOrderByIdAsc(Integer idServicio);

    Optional<ServicioRecursoJpaEntity> findByIdServicioAndIdRecurso(Integer idServicio, Integer idRecurso);

    boolean existsByIdServicioAndIdRecurso(Integer idServicio, Integer idRecurso);
}
