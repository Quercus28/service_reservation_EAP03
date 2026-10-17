package com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.entity.ReservaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservaSpringDataRepository extends JpaRepository<ReservaJpaEntity, Integer> {

    @Query("SELECT p FROM ReservaJpaEntity p WHERE p.idServicio = :idServicio AND p.estado IN :estados AND p.fechaInicio < :hasta AND :desde < p.fechaFin")
    List<ReservaJpaEntity> buscarQueSeSolapan(@Param("idServicio") Integer idServicio,
                                              @Param("estados") Collection<EstadoReserva> estados,
                                              @Param("desde") LocalDateTime desde,
                                              @Param("hasta") LocalDateTime hasta);

    List<ReservaJpaEntity> findByIdClienteAndEstadoIn(Long idCliente, Collection<EstadoReserva> estados);

    List<ReservaJpaEntity> findByIdProveedorAndEstadoIn(Integer idProveedor, Collection<EstadoReserva> estados);

    long countByIdClienteAndEstadoIn(Long idCliente, Collection<EstadoReserva> estados);

    long countByIdProveedorAndEstadoIn(Integer idProveedor, Collection<EstadoReserva> estados);
}
