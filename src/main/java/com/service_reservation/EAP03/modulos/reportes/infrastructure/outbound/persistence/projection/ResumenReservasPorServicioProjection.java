package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.projection;

/**
 * Proyección JPQL: encapsula el conteo de citas efectivas (no CANCELADAS)
 * agrupadas por servicio para un proveedor y período dados.
 * No representa una entidad JPA, solo transporta escalares de la consulta.
 */
public interface ResumenReservasPorServicioProjection {
    Integer getIdServicio();
    Long    getTotalCitas();
}
