package com.service_reservation.EAP03.modulos.reportes.domain.model;

/**
 * Value Object que el adaptador de salida entrega al Domain Service
 * con los datos crudos de un servicio:
 * - minutos totales reservados (excluyendo CANCELADO)
 * - número de citas efectivas
 * - minutos de capacidad disponible del servicio en el período
 * - metadatos básicos del servicio (nombre, duración unitaria)
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 */
public record DatosOcupacionServicio(
        Integer idServicio,
        String nombreServicio,
        int duracionMinutosServicio,
        int totalCitas,
        long minutosOcupados,
        long minutosDisponibles
) {
}
