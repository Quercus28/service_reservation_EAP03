package com.service_reservation.EAP03.modulos.availability.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.HorarioJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.repository.AgendaSpringDataRepository;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.out.AvailabilityRepository;
import com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence.ServicioJpaEntity;
import com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence.SpringDataServicioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class AvailabilityJpaAdapter implements AvailabilityRepository {

    private final SpringDataServicioRepository servicioRepository;
    private final AgendaSpringDataRepository agendaRepository;

    public AvailabilityJpaAdapter(
            SpringDataServicioRepository servicioRepository,
            AgendaSpringDataRepository agendaRepository) {
        this.servicioRepository = servicioRepository;
        this.agendaRepository = agendaRepository;
    }

    @Override
    public Optional<ServicioDisponibilidadInfo> obtenerServicioInfo(Integer servicioId) {
        if (servicioId == null) {
            return Optional.empty();
        }
        return servicioRepository.findById(servicioId)
                .map(this::mapServicioInfo);
    }

    @Override
    public List<HorarioSemanal> obtenerHorariosProveedor(Integer servicioId, Integer proveedorId) {
        if (servicioId == null) {
            return Collections.emptyList();
        }

        Optional<AgendaJpaEntity> agendaOpt = agendaRepository.findByIdServicioAndActivaTrue(servicioId);
        if (agendaOpt.isEmpty()) {
            return Collections.emptyList();
        }

        AgendaJpaEntity agenda = agendaOpt.get();
        if (proveedorId != null && !proveedorId.equals(agenda.getIdProveedor())) {
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

    private ServicioDisponibilidadInfo mapServicioInfo(ServicioJpaEntity entidad) {
        return new ServicioDisponibilidadInfo(
                entidad.getId(),
                entidad.getIdProveedor(),
                entidad.getDuracionMinutos()
        );
    }

    private HorarioSemanal mapHorarioSemanal(HorarioJpaEntity horario) {
        return new HorarioSemanal(
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );
    }
}
