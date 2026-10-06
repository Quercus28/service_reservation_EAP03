package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.AgendaMapper;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.repository.AgendaSpringDataRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PersistenciaAgendasHorariosAdapter implements IPersistenciaAgendasHorarios {
    private final AgendaSpringDataRepository repository;
    private final AgendaMapper mapper;

    public PersistenciaAgendasHorariosAdapter(
            AgendaSpringDataRepository repository,
            AgendaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Agenda guardar(Agenda agenda) {
        AgendaJpaEntity entity = mapper.toEntity(agenda);
        AgendaJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Agenda> buscarPorIdYProveedor(Long agendaId, Integer proveedorId) {
        return repository.findByIdAndIdProveedorAndActivaTrue(agendaId, proveedorId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Agenda> buscarAgendaActivaPorServicio(Integer servicioId) {
        return repository.findByIdServicioAndActivaTrue(servicioId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existeAgendaActivaPorServicio(Integer servicioId) {
        return repository.existsByIdServicioAndActivaTrue(servicioId);
    }
}
