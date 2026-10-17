package com.service_reservation.EAP03.modulos.availability.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ConsultarReservasActivasUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida (Outbound Adapter) para Availability.
 * En lugar de acoplarse directamente a bases de datos o persistencia de otros módulos,
 * consume los puertos y casos de uso públicos provistos por cada módulo:
 * - Catálogo: ObtenerServicioPorIdUseCase
 * - Agendas: IDisponibilidad
 * - Reservas: ConsultarReservasActivasUseCase
 */
@Component
public class AvailabilityJpaAdapter implements AvailabilityRepository {

    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final IDisponibilidad disponibilidadAgendasPort;
    private final ConsultarReservasActivasUseCase consultarReservasActivasUseCase;

    public AvailabilityJpaAdapter(
            ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
            IDisponibilidad disponibilidadAgendasPort,
            ConsultarReservasActivasUseCase consultarReservasActivasUseCase) {
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.disponibilidadAgendasPort = disponibilidadAgendasPort;
        this.consultarReservasActivasUseCase = consultarReservasActivasUseCase;
    }

    @Override
    public Optional<ServicioDisponibilidadInfo> obtenerServicioInfo(Integer servicioId) {
        if (servicioId == null) {
            return Optional.empty();
        }
        try {
            ServicioResponse servicio = obtenerServicioPorIdUseCase.ejecutar(servicioId);
            if (servicio == null) {
                return Optional.empty();
            }
            return Optional.of(new ServicioDisponibilidadInfo(
                    servicio.id(),
                    servicio.idProveedor(),
                    servicio.duracionMinutos()
            ));
        } catch (ServicioNoEncontradoException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<HorarioSemanal> obtenerHorariosProveedor(Integer servicioId, Integer proveedorId) {
        if (servicioId == null || proveedorId == null) {
            return Collections.emptyList();
        }

        try {
            List<Horario> horarios = disponibilidadAgendasPort.consultarHorariosDisponibles(proveedorId, servicioId);
            if (horarios == null) {
                return Collections.emptyList();
            }
            return horarios.stream()
                    .map(this::mapHorarioSemanal)
                    .toList();
        } catch (AgendaNoEncontradaException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<TimeSlot> obtenerReservasActivas(Integer servicioId, Integer proveedorId,
                                                LocalDateTime inicio, LocalDateTime fin) {
        if (servicioId == null || inicio == null || fin == null) {
            return Collections.emptyList();
        }

        List<IntervaloReservado> ocupados = consultarReservasActivasUseCase.ejecutar(servicioId, inicio, fin);
        if (ocupados == null) {
            return Collections.emptyList();
        }

        return ocupados.stream()
                .map(res -> new TimeSlot(res.inicio(), res.fin()))
                .toList();
    }

    private HorarioSemanal mapHorarioSemanal(Horario horario) {
        return new HorarioSemanal(
                horario.diaSemana(),
                horario.horaInicio(),
                horario.horaFin()
        );
    }
}
