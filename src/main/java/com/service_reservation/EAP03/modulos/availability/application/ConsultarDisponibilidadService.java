package com.service_reservation.EAP03.modulos.availability.application;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
import com.service_reservation.EAP03.modulos.availability.domain.service.DisponibilidadCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service("availabilityConsultarDisponibilidadService")
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    public static final int MAX_DIAS_RANGO = 30;

    private final AvailabilityRepository availabilityRepository;
    private final DisponibilidadCalculator disponibilidadCalculator;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public ConsultarDisponibilidadService(
            AvailabilityRepository availabilityRepository,
            DisponibilidadCalculator disponibilidadCalculator,
            Clock clock) {
        this.availabilityRepository = availabilityRepository;
        this.disponibilidadCalculator = disponibilidadCalculator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> ejecutar(Integer servicioId, LocalDate startDate, LocalDate endDate) {
        if (servicioId == null) {
            throw new DisponibilidadInvalidaException("El id del servicio es obligatorio");
        }
        if (startDate == null || endDate == null) {
            throw new DisponibilidadInvalidaException("Las fechas de inicio y fin son obligatorias");
        }

        LocalDate hoy = LocalDate.now(clock);
        if (startDate.isBefore(hoy)) {
            throw new DisponibilidadInvalidaException("La fecha de inicio (startDate) no puede ser una fecha pasada");
        }

        DateRange dateRange = new DateRange(startDate, endDate);
        if (dateRange.totalDias() > MAX_DIAS_RANGO) {
            throw new RangoFechasExcedidoException(
                    "El rango de consulta no puede superar los " + MAX_DIAS_RANGO + " días"
            );
        }

        ServicioDisponibilidadInfo servicioInfo = availabilityRepository.obtenerServicioInfo(servicioId)
                .orElseThrow(() -> new ServicioParaDisponibilidadNoEncontradoException(servicioId));

        List<HorarioSemanal> horariosProveedor = availabilityRepository.obtenerHorariosProveedor(
                servicioId,
                servicioInfo.idProveedor()
        );

        LocalDateTime rangoInicio = startDate.atStartOfDay();
        LocalDateTime rangoFin = endDate.atTime(LocalTime.MAX);

        List<TimeSlot> reservasOcupadas = availabilityRepository.obtenerReservasActivas(
                servicioId,
                servicioInfo.idProveedor(),
                rangoInicio,
                rangoFin
        );

        List<TimeSlot> slotsDisponibles = disponibilidadCalculator.calcularDisponibilidad(
                dateRange,
                horariosProveedor,
                reservasOcupadas,
                servicioInfo.duracionMinutos()
        );

        // Si la consulta inicia hoy, descartar franjas que ya hayan pasado en el tiempo actual
        LocalDateTime ahora = LocalDateTime.now(clock);
        return slotsDisponibles.stream()
                .filter(slot -> slot.start().isAfter(ahora))
                .toList();
    }
}
