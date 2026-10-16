package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.config.ReservaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CancelarReservaService implements CancelarReservaUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelarReservaService.class);

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;
    private final int anticipacionMinimaHoras;
    // El reloj ya está configurado en el módulo availability (AvailabilityConfig).
    // No se crea otro aquí: si hubiera dos relojes configurados, la aplicación no arrancaría.
    private final Clock clock;

    public CancelarReservaService(ConsultarClienteUseCase consultarClienteUseCase,
                                  ReservaRepositoryPort reservaRepositoryPort,
                                  ReservaProperties reservaProperties,
                                  Clock clock) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.anticipacionMinimaHoras = reservaProperties.getAnticipacionMinimaHorasCancelacion();
        this.clock = clock;
    }

    @Override
    @Transactional
    public Reserva ejecutar(CancelarReservaComando comando) {
        if (comando.idReserva() == null) {
            throw new DatosReservaInvalidosException("El identificador de la reserva es obligatorio");
        }

        // 1. El cliente sale siempre del usuario que inició sesión.
        Long idCliente = consultarClienteUseCase.buscarIdClientePorUsuario(comando.idUsuario())
                .orElseThrow(UsuarioNoEsClienteException::new);

        // 2. La reserva tiene que existir.
        Reserva reserva = reservaRepositoryPort.buscarPorId(comando.idReserva())
                .orElseThrow(() -> new ReservaNoEncontradaException(comando.idReserva()));

        // 3. Solo el dueño puede cancelarla. Si es de otro cliente, se deja constancia del
        //    intento con los dos números (usuario y reserva) y nada más: ni correo ni nombre
        //    ni ningún otro dato personal.
        if (!reserva.esDelCliente(idCliente)) {
            log.warn("Intento de cancelar una reserva ajena: idUsuario={}, idReserva={}",
                    comando.idUsuario(), comando.idReserva());
            throw new ReservaAjenaException(comando.idReserva());
        }

        // 4. La propia reserva revisa si todavía se puede cancelar (estado y anticipación).
        reserva.cancelar(LocalDateTime.now(clock), anticipacionMinimaHoras, comando.motivo());

        // 5. Se guarda. Al quedar CANCELADO, el horario vuelve a estar libre para otros.
        return reservaRepositoryPort.guardar(reserva);
    }
}
