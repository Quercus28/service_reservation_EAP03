package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.HorarioRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.HorarioRecursoSpringDataRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class HorarioRecursoRepositoryAdapter implements HorarioRecursoRepositoryPort {

    // dia_semana se guarda como texto, así que un ORDER BY en SQL lo ordenaría
    // alfabéticamente (DOMINGO, JUEVES, LUNES...). Se ordena aquí por el orden del enum.
    private static final Comparator<HorarioRecurso> ORDEN_SEMANAL =
            Comparator.comparing(HorarioRecurso::getDiaSemana)
                    .thenComparing(HorarioRecurso::getHoraInicio);

    private final HorarioRecursoSpringDataRepository repositorio;
    private final HorarioRecursoMapper mapper;

    public HorarioRecursoRepositoryAdapter(HorarioRecursoSpringDataRepository repositorio,
                                           HorarioRecursoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public HorarioRecurso guardar(HorarioRecurso horario) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(horario)));
    }

    @Override
    public Optional<HorarioRecurso> buscarPorId(Integer id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<HorarioRecurso> buscarPorRecurso(Integer idRecurso) {
        return repositorio.findByIdRecurso(idRecurso).stream()
                .map(mapper::aDominio)
                .sorted(ORDEN_SEMANAL)
                .toList();
    }

    @Override
    public List<HorarioRecurso> buscarPorRecursos(List<Integer> idsRecurso) {
        if (idsRecurso == null || idsRecurso.isEmpty()) {
            return List.of();
        }
        return repositorio.findByIdRecursoIn(idsRecurso).stream()
                .map(mapper::aDominio)
                .sorted(ORDEN_SEMANAL)
                .toList();
    }

    @Override
    public void eliminar(Integer id) {
        repositorio.deleteById(id);
    }
}
