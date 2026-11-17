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
 * Opera sobre la misma tabla (servicio_prestado) que el módulo de reservas,
 * pero expone solo las consultas de agregación necesarias para el reporte
 * de ocupación, manteniendo el acoplamiento mínimo entre módulos.
 *
 * La exclusión de CANCELADO se aplica a nivel de base de datos en la cláusula
 * WHERE, no en memoria, para garantizar eficiencia con grandes volúmenes.
 */
public interface ReporteReservaRepository extends JpaRepository<ReservaJpaEntity, Integer> {

    /**
     * Cuenta las citas efectivas (excluyendo CANCELADO) del proveedor agrupadas
     * por servicio, filtrando solo las reservas cuya fecha de inicio cae dentro
     * del período [inicio, fin).
     *
     * @param proveedorId      id del proveedor autenticado (del JWT)
     * @param estadoExcluido   siempre EstadoReserva.CANCELADO
     * @param inicio           inicio del período (inclusive) como LocalDateTime
     * @param fin              fin del período (exclusive, día siguiente al endDate)
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
}
