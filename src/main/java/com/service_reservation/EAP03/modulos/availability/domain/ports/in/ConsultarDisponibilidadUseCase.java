package com.service_reservation.EAP03.modulos.availability.domain.ports.in;

import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * Input Port (Caso de uso): Consultar la disponibilidad de un servicio entre dos fechas.
 */
public interface ConsultarDisponibilidadUseCase {

    /**
     * Consulta las franjas horarias disponibles para un servicio en un rango de fechas.
     *
     * @param servicioId Identificador del servicio.
     * @param startDate  Fecha de inicio de la consulta.
     * @param endDate    Fecha de fin de la consulta.
     * @return Lista de TimeSlots disponibles.
     */
    List<TimeSlot> ejecutar(Integer servicioId, LocalDate startDate, LocalDate endDate);
}
