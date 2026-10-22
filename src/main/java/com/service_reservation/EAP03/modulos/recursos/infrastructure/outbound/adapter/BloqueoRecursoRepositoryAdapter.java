package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.BloqueoRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.BloqueoRecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.BloqueoRecursoSpringDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class BloqueoRecursoRepositoryAdapter implements BloqueoRecursoRepositoryPort {

    private final BloqueoRecursoSpringDataRepository repositorio;
    private final BloqueoRecursoMapper mapper;

    public BloqueoRecursoRepositoryAdapter(BloqueoRecursoSpringDataRepository repositorio,
                                           BloqueoRecursoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public BloqueoRecurso guardar(BloqueoRecurso bloqueo) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(bloqueo)));
    }

    @Override
    public Optional<BloqueoRecurso> buscarPorId(Integer id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<BloqueoRecurso> buscarQueSeSolapan(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        return repositorio.buscarQueSeSolapan(idRecurso, desde, hasta).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public ResultadoPaginado<BloqueoRecurso> buscarPorRecurso(Integer idRecurso, int pagina, int tamano) {
        Page<BloqueoRecursoJpaEntity> pagi = repositorio.findByIdRecursoOrderByFechaInicioAsc(
                idRecurso, PageRequest.of(pagina, tamano));

        List<BloqueoRecurso> contenido = pagi.getContent().stream()
                .map(mapper::aDominio)
                .toList();

        return new ResultadoPaginado<>(contenido, pagina, tamano, pagi.getTotalElements());
    }

    @Override
    public void eliminar(Integer id) {
        repositorio.deleteById(id);
    }

    @Override
    public List<BloqueoRecurso> buscarQueSeSolapanDeRecursos(Collection<Integer> idsRecurso,
                                                            LocalDateTime desde, LocalDateTime hasta) {
        return repositorio.buscarQueSeSolapanDeRecursos(idsRecurso, desde, hasta).stream()
                .map(mapper::aDominio)
                .toList();
    }
}
