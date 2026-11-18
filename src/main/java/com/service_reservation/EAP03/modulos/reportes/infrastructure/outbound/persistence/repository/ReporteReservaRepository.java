package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.repository;

import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.projection.ResumenReservasPorServicioProjection;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.entity.ReservaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de lectura optimizado para el módulo de reportes.
 * Realiza consultas agregadas sobre la tabla de reservas mediante Spring Data JPA.
 */
public interface ReporteReservaRepository extends JpaRepository<ReservaJpaEntity, Integer> {

    /**
     * Cuenta las citas efectivas (excluyendo CANCELADO) del proveedor agrupadas
     * por servicio, filtrando reservas en el período [inicio, fin).
     */
    @Query("""
            SELECT r.idServicio  AS idServicio,
                   COUNT(r)      AS totalCitas
            FROM   ReservaJpaEntity r
            WHERE  r.idProveedor  = :proveedorId
              AND  r.estado      <> :estadoExcluido
              AND  r.fechaInicio >= :inicio
              AND  r.fechaInicio  < :fin
            GROUP BY r.idServicio
            """)
    List<ResumenReservasPorServicioProjection> contarCitasPorServicio(
            @Param("proveedorId")    Integer        proveedorId,
            @Param("estadoExcluido") EstadoReserva  estadoExcluido,
            @Param("inicio")         LocalDateTime  inicio,
            @Param("fin")            LocalDateTime  fin
    );

    /**
     * Cuenta las citas canceladas del proveedor agrupadas por servicio,
     * filtrando reservas en el período [inicio, fin).
     */
    @Query("""
            SELECT r.idServicio  AS idServicio,
                   COUNT(r)      AS totalCitas
            FROM   ReservaJpaEntity r
            WHERE  r.idProveedor  = :proveedorId
              AND  r.estado       = :estadoCancelado
              AND  r.fechaInicio >= :inicio
              AND  r.fechaInicio  < :fin
            GROUP BY r.idServicio
            """)
    List<ResumenReservasPorServicioProjection> contarCanceladasPorServicio(
            @Param("proveedorId")     Integer        proveedorId,
            @Param("estadoCancelado") EstadoReserva  estadoCancelado,
            @Param("inicio")          LocalDateTime  inicio,
            @Param("fin")             LocalDateTime  fin
    );
}
