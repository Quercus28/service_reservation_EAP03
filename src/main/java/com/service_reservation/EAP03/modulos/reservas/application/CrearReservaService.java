package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
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
    // Bean declarado en AvailabilityConfig; no se declara otro porque dos beans Clock
    // impedirían que la aplicación arranque.
    private final Clock clock;

    public CrearReservaService(ConsultarClienteUseCase consultarClienteUseCase,
                               ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
                               IDisponibilidad disponibilidadAgenda,
                               ReservaRepositoryPort reservaRepositoryPort,
                               Clock clock) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.disponibilidadAgenda = disponibilidadAgenda;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.clock = clock;
    }

    // No hay una fila propia del módulo que bloquear (en recursos se bloquea la fila del
    // recurso), así que se usa aislamiento SERIALIZABLE: si dos reservas que se cruzan se
    // guardan al mismo tiempo, la base de datos solo deja confirmar una y la otra falla con
    // un error de serialización (ReservaExceptionHandler lo responde como 409).
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reserva ejecutar(CrearReservaComando comando) {
        // 1. El cliente sale siempre del usuario autenticado, nunca del body.
        Long idCliente = consultarClienteUseCase.buscarIdClientePorUsuario(comando.idUsuario())
                .orElseThrow(UsuarioNoEsClienteException::new);

        // 2. Servicio desde catálogo, con el error traducido al formato de reservas.
        ServicioResponse servicio;
        try {
            servicio = obtenerServicioPorIdUseCase.ejecutar(comando.idServicio());
        } catch (ServicioNoEncontradoException e) {
            throw new ServicioNoReservableException(comando.idServicio());
        }

        // 3. La reserva dura lo que dura el servicio.
        LocalDateTime fechaInicio = comando.fechaInicio();
        LocalDateTime fechaFin = fechaInicio.plusMinutes(servicio.duracionMinutos());

        // 4. Valida que no sea en el pasado y que el rango sea coherente.
        Reserva nueva = Reserva.nueva(
                idCliente,
                servicio.id(),
                servicio.idProveedor(),
                fechaInicio,
                fechaFin,
                comando.notas(),
                LocalDateTime.now(clock)
        );

        // 5. Tiene que caber completa en una franja de la agenda del servicio.
        List<Horario> horarios;
        try {
            horarios = disponibilidadAgenda.consultarHorariosDisponibles(servicio.idProveedor(), servicio.id());
        } catch (AgendaNoEncontradaException e) {
            throw HorarioNoDisponibleException.sinAgenda(servicio.id());
        }
        if (horarios.stream().noneMatch(h -> cabeEnHorario(h, fechaInicio, fechaFin))) {
            throw HorarioNoDisponibleException.fueraDeAgenda(servicio.id(), fechaInicio, fechaFin);
        }

        // 6. No puede cruzarse con otra reserva activa del mismo servicio.
        boolean ocupado = reservaRepositoryPort.buscarActivasQueSeSolapan(servicio.id(), fechaInicio, fechaFin)
                .stream()
                .anyMatch(r -> r.seSolapaCon(fechaInicio, fechaFin));
        if (ocupado) {
            throw HorarioNoDisponibleException.yaReservado(servicio.id(), fechaInicio, fechaFin);
        }

        // 7.
        return reservaRepositoryPort.guardar(nueva);
    }

    // Mismo día calendario, ese día coincide con el de la franja y el rango completo cae
    // dentro de [horaInicio, horaFin].
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

    // Se compara por nombre para no depender del enum DiaSemana interno de agendas_horarios.
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
