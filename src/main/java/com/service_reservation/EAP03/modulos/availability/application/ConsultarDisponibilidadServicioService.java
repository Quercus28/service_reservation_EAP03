package com.service_reservation.EAP03.modulos.availability.application;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.in.ConsultarDisponibilidadServicioUseCase;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
import com.service_reservation.EAP03.modulos.availability.domain.service.DisponibilidadCalculator;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Turno;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.FiltrarTurnosConRecursosDisponiblesUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ConsultarDisponibilidadServicioService implements ConsultarDisponibilidadServicioUseCase {

    public static final int MAX_DIAS_RANGO = 30;

    private final AvailabilityRepository availabilityRepository;
    private final DisponibilidadCalculator disponibilidadCalculator;
    private final FiltrarTurnosConRecursosDisponiblesUseCase filtrarTurnosConRecursosDisponiblesUseCase;
    private final Clock clock;

    @Autowired
    public ConsultarDisponibilidadServicioService(
            AvailabilityRepository availabilityRepository,
            DisponibilidadCalculator disponibilidadCalculator,
            FiltrarTurnosConRecursosDisponiblesUseCase filtrarTurnosConRecursosDisponiblesUseCase,
            Clock clock) {
        this.availabilityRepository = availabilityRepository;
        this.disponibilidadCalculator = disponibilidadCalculator;
        this.filtrarTurnosConRecursosDisponiblesUseCase = filtrarTurnosConRecursosDisponiblesUseCase;
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

        // Quitar los turnos en los que falta algún recurso obligatorio del servicio
        List<TimeSlot> slotsConRecursos = filtrarTurnosConRecursosDisponiblesUseCase.ejecutar(
                        servicioId,
                        slotsDisponibles.stream().map(slot -> new Turno(slot.start(), slot.end())).toList()
                ).stream()
                .map(turno -> new TimeSlot(turno.inicio(), turno.fin()))
                .toList();

        // Si la consulta inicia hoy, descartar franjas que ya hayan pasado en el tiempo actual
        LocalDateTime ahora = LocalDateTime.now(clock);
        return slotsConRecursos.stream()
                .filter(slot -> slot.start().isAfter(ahora))
                .toList();
    }
}
