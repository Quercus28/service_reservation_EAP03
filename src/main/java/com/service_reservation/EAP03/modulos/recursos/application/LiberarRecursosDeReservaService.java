package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.LiberarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class LiberarRecursosDeReservaService implements LiberarRecursosDeReservaUseCase {

    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;
    // El reloj ya está configurado en el módulo availability (AvailabilityConfig).
    private final Clock clock;

    public LiberarRecursosDeReservaService(RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort,
                                           Clock clock) {
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
        this.clock = clock;
    }

    // Si quien llama ya tiene una transacción abierta (la cancelación de la reserva), se usa
    // esa misma: si la cancelación falla, los recursos tampoco se liberan.
    @Override
    @Transactional
    public void ejecutar(Integer idReserva) {
        if (idReserva == null) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        for (RecursoPrestado prestamo : recursoPrestadoRepositoryPort.buscarActivosDeReserva(idReserva)) {
            prestamo.liberar(ahora);
            recursoPrestadoRepositoryPort.guardar(prestamo);
        }
    }
}
