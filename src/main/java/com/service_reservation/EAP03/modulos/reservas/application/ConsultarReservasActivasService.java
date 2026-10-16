package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ConsultarReservasActivasUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultarReservasActivasService implements ConsultarReservasActivasUseCase {

    private final ReservaRepositoryPort reservaRepositoryPort;

    public ConsultarReservasActivasService(ReservaRepositoryPort reservaRepositoryPort) {
        this.reservaRepositoryPort = reservaRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntervaloReservado> ejecutar(Integer idServicio, LocalDateTime desde, LocalDateTime hasta) {
        return reservaRepositoryPort.buscarActivasQueSeSolapan(idServicio, desde, hasta).stream()
                .filter(r -> r.seSolapaCon(desde, hasta))
                .map(r -> new IntervaloReservado(r.getFechaInicio(), r.getFechaFin()))
                .toList();
    }
}
