package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDominioException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoOpcionalNoValidoException;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ApartarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HoraFueraDeTurnoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoOpcionalInvalidoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ReglaDeTurnos;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CrearReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrearReservaService implements CrearReservaUseCase {

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final IDisponibilidad disponibilidadAgenda;
    private final ReservaRepositoryPort reservaRepositoryPort;
    private final ApartarRecursosDeReservaUseCase apartarRecursosDeReservaUseCase;
    // El reloj ya está configurado en el módulo availability (AvailabilityConfig).
    // No se crea otro aquí: si hubiera dos relojes configurados, la aplicación no arrancaría.
    private final Clock clock;

    public CrearReservaService(ConsultarClienteUseCase consultarClienteUseCase,
                               ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
                               IDisponibilidad disponibilidadAgenda,
                               ReservaRepositoryPort reservaRepositoryPort,
                               ApartarRecursosDeReservaUseCase apartarRecursosDeReservaUseCase,
                               Clock clock) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.disponibilidadAgenda = disponibilidadAgenda;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.apartarRecursosDeReservaUseCase = apartarRecursosDeReservaUseCase;
        this.clock = clock;
    }

    // Problema: dos clientes pueden pedir el mismo horario en el mismo instante. Los dos
    // revisan, los dos ven el horario libre y los dos guardarían su reserva.
    // En recursos esto se evita "bloqueando" el registro del recurso mientras se presta,
    // pero aquí no hay un registro propio que bloquear. Por eso se le pide a la base de
    // datos el modo más estricto (SERIALIZABLE): si detecta que dos reservas que se cruzan
    // se están guardando a la vez, deja pasar solo una y la otra falla. Ese fallo se le
    // responde al cliente como 409 en ReservaExceptionHandler.
    // Ojo: PostgreSQL sí hace esta detección; la base de pruebas (H2) no, así que en las
    // pruebas automáticas esta protección no se puede comprobar.
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reserva ejecutar(CrearReservaComando comando) {
        // 1. El cliente sale siempre del usuario que inició sesión, nunca de los datos que
        //    manda la petición (así nadie puede reservar a nombre de otro).
        Long idCliente = consultarClienteUseCase.buscarIdClientePorUsuario(comando.idUsuario())
                .orElseThrow(UsuarioNoEsClienteException::new);

        // 2. Se pide el servicio al módulo de catálogo. Si no existe, se responde con el
        //    error propio de reservas para que el mensaje tenga el mismo formato.
        ServicioResponse servicio;
        try {
            servicio = obtenerServicioPorIdUseCase.ejecutar(comando.idServicio());
        } catch (ServicioNoEncontradoException e) {
            throw new ServicioNoReservableException(comando.idServicio());
        }

        // 3. La reserva dura lo que dura el servicio.
        LocalDateTime fechaInicio = comando.fechaInicio();
        LocalDateTime fechaFin = fechaInicio.plusMinutes(servicio.duracionMinutos());

        // 4. Se arma la reserva; aquí se rechaza si la fecha ya pasó.
        Reserva nueva = Reserva.nueva(
                idCliente,
                servicio.id(),
                servicio.idProveedor(),
                fechaInicio,
                fechaFin,
                comando.notas(),
                LocalDateTime.now(clock)
        );

        // 5. La reserva tiene que caber completa dentro de uno de los horarios de atención
        //    de la agenda del servicio.
        List<Horario> horarios;
        try {
            horarios = disponibilidadAgenda.consultarHorariosDisponibles(servicio.idProveedor(), servicio.id());
        } catch (AgendaNoEncontradaException e) {
            throw HorarioNoDisponibleException.sinAgenda(servicio.id());
        }
        List<Horario> horariosDondeCabe = horarios.stream()
                .filter(h -> cabeEnHorario(h, fechaInicio, fechaFin))
                .toList();
        if (horariosDondeCabe.isEmpty()) {
            throw HorarioNoDisponibleException.fueraDeAgenda(servicio.id(), fechaInicio, fechaFin);
        }

        // 5b. Además tiene que empezar justo en uno de los turnos de ese horario (los mismos
        //     que muestra la consulta de disponibilidad). Si no, por ejemplo una reserva a las
        //     8:30 en turnos de 60 minutos desde las 8:00 taparía dos turnos y escondería
        //     horarios que sí están libres.
        int duracion = servicio.duracionMinutos();
        if (horariosDondeCabe.stream().noneMatch(
                h -> ReglaDeTurnos.caeEnTurno(h.horaInicio(), fechaInicio.toLocalTime(), duracion))) {
            throw new HoraFueraDeTurnoException(duracion, horariosDondeCabe.get(0).horaInicio());
        }

        // 6. No puede cruzarse con otra reserva vigente del mismo servicio.
        boolean ocupado = reservaRepositoryPort.buscarActivasQueSeSolapan(servicio.id(), fechaInicio, fechaFin)
                .stream()
                .anyMatch(r -> r.seSolapaCon(fechaInicio, fechaFin));
        if (ocupado) {
            throw HorarioNoDisponibleException.yaReservado(servicio.id(), fechaInicio, fechaFin);
        }

        // 7. Todo en orden: se guarda.
        Reserva guardada = reservaRepositoryPort.guardar(nueva);

        // 8. Se apartan los recursos adicionales que pidió el cliente (si no pidió ninguno, no
        //    se aparta nada). Va después de guardar porque cada recurso apartado queda ligado al
        //    número de la reserva. Si alguno no se puede apartar, se lanza el error y, como
        //    todo está en la misma transacción, la reserva tampoco queda guardada.
        //    Los errores de recursos se pasan a errores de reservas para que la respuesta
        //    tenga el mismo formato que las demás.
        try {
            apartarRecursosDeReservaUseCase.ejecutar(guardada.getId(), servicio.id(), fechaInicio, fechaFin,
                    comando.recursosOpcionales());
        } catch (RecursoOpcionalNoValidoException e) {
            throw new RecursoOpcionalInvalidoException(e.getMessage());
        } catch (RecursoDominioException e) {
            throw new RecursoNoDisponibleException(e.getMessage());
        }
        return guardada;
    }

    // La reserva cabe en el horario si empieza y termina el mismo día, ese día de la semana
    // es el del horario, y empieza y termina dentro de la hora de inicio y fin del horario.
    private static boolean cabeEnHorario(Horario horario, LocalDateTime inicio, LocalDateTime fin) {
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            return false;
        }
        if (!diaSemanaEnEspanol(inicio.toLocalDate().getDayOfWeek()).equals(horario.diaSemana().name())) {
            return false;
        }
        return !inicio.toLocalTime().isBefore(horario.horaInicio())
                && !fin.toLocalTime().isAfter(horario.horaFin());
    }

    // Se compara el día por su nombre ("LUNES", "MARTES"...) para no usar la lista de días
    // interna del módulo de agendas.
    private static String diaSemanaEnEspanol(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}
