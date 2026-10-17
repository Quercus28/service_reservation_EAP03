package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cuenta las reservas activas (RESERVADO o EN_PROCESO) de un cliente. Lo usa el módulo admin.
// El usuario se pasa a cliente con el caso de uso de identidad, no leyendo sus tablas.
@Service
public class ContarReservasActivasDeClienteService implements ContarReservasActivasDeClienteUseCase {

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;

    public ContarReservasActivasDeClienteService(ConsultarClienteUseCase consultarClienteUseCase,
                                                 ReservaRepositoryPort reservaRepositoryPort) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public int ejecutar(Long idUsuario) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarClienteUseCase.buscarIdClientePorUsuario(idUsuario)
                .map(reservaRepositoryPort::contarActivasDeCliente)
                .orElse(0);
    }
}
