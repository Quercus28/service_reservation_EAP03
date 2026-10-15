package com.service_reservation.EAP03.modulos.availability.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
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
 * En lugar de acoplarse directamente a repositorios JPA ajenos,
 * consume los puertos y casos de uso públicos de los módulos de catálogos y agendas.
 */
@Component
public class AvailabilityJpaAdapter implements AvailabilityRepository {

    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final IPersistenciaAgendasHorarios persistenciaAgendasHorarios;

    public AvailabilityJpaAdapter(
            ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
            IPersistenciaAgendasHorarios persistenciaAgendasHorarios) {
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.persistenciaAgendasHorarios = persistenciaAgendasHorarios;
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
        if (servicioId == null) {
            return Collections.emptyList();
        }

        Optional<Agenda> agendaOpt = persistenciaAgendasHorarios.buscarAgendaActivaPorServicio(servicioId);
        if (agendaOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Agenda agenda = agendaOpt.get();
        if (proveedorId != null && !proveedorId.equals(agenda.getProveedorId())) {
            return Collections.emptyList();
        }

        return agenda.getHorarios().stream()
                .map(this::mapHorarioSemanal)
                .toList();
    }

    @Override
    public List<TimeSlot> obtenerReservasActivas(Integer servicioId, Integer proveedorId,
                                                LocalDateTime inicio, LocalDateTime fin) {
        // En el alcance de la HU07 se consultan los horarios disponibles de las agendas del servicio/proveedor.
        // Las reservas activas se integran a través del puerto cuando el módulo de reservas esté disponible.
        return Collections.emptyList();
    }

    private HorarioSemanal mapHorarioSemanal(Horario horario) {
        return new HorarioSemanal(
                horario.diaSemana(),
                horario.horaInicio(),
                horario.horaFin()
        );
    }
}
