package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.RecursoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.RecursoSpringDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RecursoRepositoryAdapter implements RecursoRepositoryPort {

    private final RecursoSpringDataRepository repositorio;
    private final RecursoMapper mapper;

    public RecursoRepositoryAdapter(RecursoSpringDataRepository repositorio, RecursoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Recurso guardar(Recurso recurso) {
        RecursoJpaEntity guardado = repositorio.save(mapper.aEntidad(recurso));
        return mapper.aDominio(guardado);
    }

    @Override
    public Optional<Recurso> buscarPorId(Integer id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public Optional<Recurso> buscarPorIdConBloqueo(Integer id) {
        return repositorio.buscarPorIdConBloqueo(id).map(mapper::aDominio);
    }

    @Override
    public ResultadoPaginado<Recurso> buscarActivosPorProveedor(Integer idProveedor, int pagina, int tamano) {
        Page<RecursoJpaEntity> pagi = repositorio.findByIdProveedorAndEliminadoEnIsNull(
                idProveedor, PageRequest.of(pagina, tamano));

        List<Recurso> contenido = pagi.getContent().stream()
                .map(mapper::aDominio)
                .toList();

        return new ResultadoPaginado<>(contenido, pagina, tamano, pagi.getTotalElements());
    }

    @Override
    public boolean existePorId(Integer id) {
        return repositorio.existsById(id);
    }
}
