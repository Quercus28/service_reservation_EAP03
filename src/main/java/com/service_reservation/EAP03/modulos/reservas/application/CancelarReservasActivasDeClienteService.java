package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.LiberarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

// Cancela todas las reservas activas (RESERVADO o EN_PROCESO) de un cliente. Lo usa el módulo
// admin cuando le quita los permisos. Es una cancelación del administrador: no aplica la
// regla de las horas de anticipación que sí tiene el cliente.
@Service
public class CancelarReservasActivasDeClienteService implements CancelarReservasActivasDeClienteUseCase {

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;
    private final LiberarRecursosDeReservaUseCase liberarRecursosDeReservaUseCase;
    // El reloj ya está configurado en el módulo availability (AvailabilityConfig).
    private final Clock clock;

    public CancelarReservasActivasDeClienteService(ConsultarClienteUseCase consultarClienteUseCase,
                                                   ReservaRepositoryPort reservaRepositoryPort,
                                                   LiberarRecursosDeReservaUseCase liberarRecursosDeReservaUseCase,
                                                   Clock clock) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.liberarRecursosDeReservaUseCase = liberarRecursosDeReservaUseCase;
        this.clock = clock;
    }

    // Las cancelaciones van en una sola transacción: si falla una, no se cancela ninguna. Si quien llama
    // ya tiene una transacción abierta (por ejemplo la revocación del admin), se usa esa misma.
    @Override
    @Transactional
    public int ejecutar(Long idUsuario, String motivo) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarClienteUseCase.buscarIdClientePorUsuario(idUsuario)
                .map(id -> cancelarTodas(reservaRepositoryPort.buscarActivasDeCliente(id), motivo))
                .orElse(0);
    }

    // Cada reserva se cancela con la regla del dominio (Reserva) y se guarda con la misma
    // hora de cancelación para todas. Después se devuelven los recursos que tenía apartados.
    private int cancelarTodas(List<Reserva> reservas, String motivo) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        for (Reserva reserva : reservas) {
            reserva.cancelarPorAdministrador(ahora, motivo);
            reservaRepositoryPort.guardar(reserva);
            liberarRecursosDeReservaUseCase.ejecutar(reserva.getId());
        }
        return reservas.size();
    }
}
