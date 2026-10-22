package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.RecursoPrestadoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.RecursoPrestadoSpringDataRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Component
public class RecursoPrestadoRepositoryAdapter implements RecursoPrestadoRepositoryPort {

    private final RecursoPrestadoSpringDataRepository repositorio;
    private final RecursoPrestadoMapper mapper;

    public RecursoPrestadoRepositoryAdapter(RecursoPrestadoSpringDataRepository repositorio,
                                            RecursoPrestadoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public RecursoPrestado guardar(RecursoPrestado recursoPrestado) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(recursoPrestado)));
    }

    @Override
    public List<RecursoPrestado> buscarQueSeSolapan(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        return repositorio.buscarQueSeSolapan(idRecurso, desde, hasta).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public List<RecursoPrestado> buscarActivosDeReserva(Integer idServicioPrestado) {
        return repositorio.findByIdServicioPrestadoAndLiberadoEnIsNull(idServicioPrestado).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public List<RecursoPrestado> buscarQueSeSolapanDeRecursos(Collection<Integer> idsRecurso,
                                                             LocalDateTime desde, LocalDateTime hasta) {
        return repositorio.buscarQueSeSolapanDeRecursos(idsRecurso, desde, hasta).stream()
                .map(mapper::aDominio)
                .toList();
    }
}
