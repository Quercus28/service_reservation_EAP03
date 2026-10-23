package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoYaAsociadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.ServicioRecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.ServicioRecursoSpringDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ServicioRecursoRepositoryAdapter implements ServicioRecursoRepositoryPort {

    private final ServicioRecursoSpringDataRepository repositorio;
    private final ServicioRecursoMapper mapper;

    public ServicioRecursoRepositoryAdapter(ServicioRecursoSpringDataRepository repositorio,
                                            ServicioRecursoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public ServicioRecurso guardar(ServicioRecurso servicioRecurso) {
        // Si dos personas asocian el mismo recurso al mismo servicio a la vez, la base de
        // datos solo deja pasar una. A la otra se le responde como si ya estuviera asociado.
        try {
            return mapper.aDominio(repositorio.saveAndFlush(mapper.aEntidad(servicioRecurso)));
        } catch (DataIntegrityViolationException e) {
            throw new RecursoYaAsociadoException(servicioRecurso.getIdRecurso(), servicioRecurso.getIdServicio());
        }
    }

    @Override
    public Optional<ServicioRecurso> buscarPorServicioYRecurso(Integer idServicio, Integer idRecurso) {
        return repositorio.findByIdServicioAndIdRecurso(idServicio, idRecurso).map(mapper::aDominio);
    }

    @Override
    public boolean existePorServicioYRecurso(Integer idServicio, Integer idRecurso) {
        return repositorio.existsByIdServicioAndIdRecurso(idServicio, idRecurso);
    }

    @Override
    public List<ServicioRecurso> buscarPorServicio(Integer idServicio) {
        return repositorio.findByIdServicioOrderByIdAsc(idServicio).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public void eliminar(Integer id) {
        repositorio.deleteById(id);
    }
}
