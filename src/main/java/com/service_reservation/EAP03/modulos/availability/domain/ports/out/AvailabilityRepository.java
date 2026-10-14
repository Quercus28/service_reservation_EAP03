package com.service_reservation.EAP03.modulos.availability.domain.ports.out;

import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output Port: Repositorio / Puerto de salida para consultar información del servicio,
 * configuración de horarios del proveedor y reservas existentes en la franja solicitada.
 */
public interface AvailabilityRepository {

    /**
     * Obtiene la configuración del servicio (proveedor, duración).
     *
     * @param servicioId Identificador del servicio.
     * @return Optional con ServicioDisponibilidadInfo si existe.
     */
    Optional<ServicioDisponibilidadInfo> obtenerServicioInfo(Integer servicioId);

    /**
     * Obtiene los horarios semanales activos del proveedor para el servicio o agenda.
     *
     * @param servicioId  Identificador del servicio.
     * @param proveedorId Identificador del proveedor.
     * @return Lista de horarios semanales configurados.
     */
    List<HorarioSemanal> obtenerHorariosProveedor(Integer servicioId, Integer proveedorId);

    /**
     * Obtiene las reservas activas (o bloqueos) que se solapan con el rango solicitado.
     *
     * @param servicioId  Identificador del servicio.
     * @param proveedorId Identificador del proveedor.
     * @param inicio      Fecha/hora de inicio del rango.
     * @param fin         Fecha/hora de fin del rango.
     * @return Lista de TimeSlots correspondientes a reservas ocupadas.
     */
    List<TimeSlot> obtenerReservasActivas(Integer servicioId, Integer proveedorId, LocalDateTime inicio, LocalDateTime fin);
}
