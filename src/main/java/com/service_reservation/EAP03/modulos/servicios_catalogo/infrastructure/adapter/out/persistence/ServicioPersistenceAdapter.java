package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ServicioPersistenceAdapter implements ServicioRepository {

    private final SpringDataServicioRepository springDataServicioRepository;
    private final ServicioMapper servicioMapper;

    public ServicioPersistenceAdapter(SpringDataServicioRepository springDataServicioRepository, ServicioMapper servicioMapper) {
        this.springDataServicioRepository = Objects.requireNonNull(springDataServicioRepository, "springDataServicioRepository no puede ser nulo");
        this.servicioMapper = Objects.requireNonNull(servicioMapper, "servicioMapper no puede ser nulo");
    }

    @Override
    public Servicio guardar(Servicio servicio) {
        Objects.requireNonNull(servicio, "El servicio a guardar no puede ser nulo");
        ServicioJpaEntity entity = servicioMapper.toJpaEntity(servicio);
        ServicioJpaEntity savedEntity = springDataServicioRepository.save(entity);
        return servicioMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Servicio> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataServicioRepository.findById(id)
                .map(servicioMapper::toDomain);
    }

    @Override
    public List<Servicio> buscarPorProveedor(Integer idProveedor) {
        if (idProveedor == null) {
            return List.of();
        }
        return springDataServicioRepository.findByIdProveedor(idProveedor).stream()
                .map(servicioMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorId(Integer id) {
        if (id == null) {
            return false;
        }
        return springDataServicioRepository.existsById(id);
    }

    @Override
    public void eliminarPorId(Integer id) {
        if (id != null) {
            springDataServicioRepository.deleteById(id);
        }
    }
}
