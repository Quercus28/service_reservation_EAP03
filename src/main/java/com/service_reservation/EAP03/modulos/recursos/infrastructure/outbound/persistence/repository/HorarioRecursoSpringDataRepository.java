package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.HorarioRecursoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HorarioRecursoSpringDataRepository extends JpaRepository<HorarioRecursoJpaEntity, Integer> {

    List<HorarioRecursoJpaEntity> findByIdRecurso(Integer idRecurso);

    List<HorarioRecursoJpaEntity> findByIdRecursoIn(Collection<Integer> idsRecurso);
}
