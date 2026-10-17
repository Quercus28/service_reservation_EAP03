package com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.ReservaMapper;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.repository.ReservaSpringDataRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ReservaRepositoryAdapter implements ReservaRepositoryPort {

    // Qué estados cuentan como "vigentes" se decide solo en EstadoReserva.esActiva(); aquí se
    // reutiliza esa regla para no repetirla.
    private static final List<EstadoReserva> ESTADOS_ACTIVOS = Arrays.stream(EstadoReserva.values())
            .filter(EstadoReserva::esActiva)
            .toList();

    private final ReservaSpringDataRepository repositorio;
    private final ReservaMapper mapper;

    public ReservaRepositoryAdapter(ReservaSpringDataRepository repositorio, ReservaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Reserva guardar(Reserva reserva) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(reserva)));
    }

    @Override
    public Optional<Reserva> buscarPorId(Integer id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Reserva> buscarActivasQueSeSolapan(Integer idServicio, LocalDateTime desde, LocalDateTime hasta) {
        return repositorio.buscarQueSeSolapan(idServicio, ESTADOS_ACTIVOS, desde, hasta).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public List<Reserva> buscarActivasDeCliente(Long idCliente) {
        return repositorio.findByIdClienteAndEstadoIn(idCliente, ESTADOS_ACTIVOS).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public List<Reserva> buscarActivasDeProveedor(Integer idProveedor) {
        return repositorio.findByIdProveedorAndEstadoIn(idProveedor, ESTADOS_ACTIVOS).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public int contarActivasDeCliente(Long idCliente) {
        return (int) repositorio.countByIdClienteAndEstadoIn(idCliente, ESTADOS_ACTIVOS);
    }

    @Override
    public int contarActivasDeProveedor(Integer idProveedor) {
        return (int) repositorio.countByIdProveedorAndEstadoIn(idProveedor, ESTADOS_ACTIVOS);
    }
}
