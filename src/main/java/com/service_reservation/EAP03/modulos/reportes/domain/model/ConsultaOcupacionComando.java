package com.service_reservation.EAP03.modulos.reportes.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Comando de entrada al caso de uso de reporte de ocupación.
 * Agrupa los parámetros necesarios evitando métodos con más de 5 parámetros.
 * El proveedor siempre proviene del contexto de seguridad (nunca del cliente).
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 */
public record ConsultaOcupacionComando(
        Integer proveedorId,
        LocalDate startDate,
        LocalDate endDate
) {
    public ConsultaOcupacionComando {
        Objects.requireNonNull(proveedorId, "El id del proveedor es obligatorio");
        Objects.requireNonNull(startDate,   "La fecha de inicio es obligatoria");
        Objects.requireNonNull(endDate,     "La fecha de fin es obligatoria");
    }
}
