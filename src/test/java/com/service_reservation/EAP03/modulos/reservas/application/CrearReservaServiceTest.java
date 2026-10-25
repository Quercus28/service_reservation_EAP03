package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoOpcionalNoValidoException;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ApartarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.FechaEnElPasadoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HoraFueraDeTurnoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HorarioNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoNoDisponibleException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.RecursoOpcionalInvalidoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ServicioNoReservableException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearReservaServiceTest {

    private static final Long ID_USUARIO = 20L;
    private static final Long ID_CLIENTE = 3L;
    private static final Integer ID_SERVICIO = 10;
    private static final Integer ID_PROVEEDOR = 7;
    private static final LocalDateTime LUNES_NUEVE = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final Horario LUNES_DE_OCHO_A_DOCE = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));
    private static final List<Integer> OPCIONALES = List.of(4);

    @Mock private ConsultarClienteUseCase consultarClienteUseCase;
    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    @Mock private IDisponibilidad disponibilidadAgenda;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private ApartarRecursosDeReservaUseCase apartarRecursosDeReservaUseCase;

    private CrearReservaService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        service = new CrearReservaService(consultarClienteUseCase, obtenerServicioPorIdUseCase, disponibilidadAgenda,
                reservaRepositoryPort, apartarRecursosDeReservaUseCase, reloj);
    }

    private static CrearReservaComando command(LocalDateTime inicio) {
        return new CrearReservaComando(ID_USUARIO, ID_SERVICIO, inicio, "Traer toalla", OPCIONALES);
    }

    private void clientAndService() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_CLIENTE));
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(new ServicioResponse(ID_SERVICIO, ID_PROVEEDOR, "Masaje", 60, null));
    }

    private void agendaWith(Horario... horarios) {
        when(disponibilidadAgenda.consultarHorariosDisponibles(ID_PROVEEDOR, ID_SERVICIO)).thenReturn(List.of(horarios));
    }

    private void freeSlotAndSavingWithId() {
        when(reservaRepositoryPort.buscarActivasQueSeSolapan(any(), any(), any())).thenReturn(List.of());
        when(reservaRepositoryPort.guardar(any())).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            return Reserva.reconstruir(50, r.getIdCliente(), r.getIdServicio(), r.getIdProveedor(), r.getFechaInicio(),
                    r.getFechaFin(), r.getEstado(), r.getNotas(), r.getCreatedAt(), null, null);
        });
    }

    @Test
    @DisplayName("Crea la reserva del cliente autenticado con la duración del servicio y aparta sus recursos")
    void createsReservation() {
        // Arrange
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);
        freeSlotAndSavingWithId();

        // Act
        Reserva reserva = service.ejecutar(command(LUNES_NUEVE));

        // Assert
        assertEquals(50, reserva.getId());
        assertEquals(ID_CLIENTE, reserva.getIdCliente());
        assertEquals(ID_PROVEEDOR, reserva.getIdProveedor());
        assertEquals(LUNES_NUEVE.plusMinutes(60), reserva.getFechaFin());
        assertEquals(EstadoReserva.RESERVADO, reserva.getEstado());
        assertEquals(LocalDateTime.of(2026, 10, 24, 12, 0), reserva.getCreatedAt());
        verify(apartarRecursosDeReservaUseCase).ejecutar(50, ID_SERVICIO, LUNES_NUEVE, LUNES_NUEVE.plusMinutes(60), OPCIONALES);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "2026-11-02, LUNES",
            "2026-11-03, MARTES",
            "2026-11-04, MIERCOLES",
            "2026-11-05, JUEVES",
            "2026-11-06, VIERNES",
            "2026-11-07, SABADO",
            "2026-11-08, DOMINGO"
    })
    @DisplayName("Reconoce el horario de cada día de la semana")
    void matchesScheduleOfEveryWeekDay(LocalDate fecha, DiaSemana dia) {
        // Arrange
        clientAndService();
        agendaWith(new Horario(1L, dia, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        freeSlotAndSavingWithId();

        // Act
        Reserva reserva = service.ejecutar(command(fecha.atTime(10, 0)));

        // Assert
        assertEquals(fecha.atTime(10, 0), reserva.getFechaInicio());
    }

    @Test
    @DisplayName("Una reserva que termina justo al cierre del horario es válida")
    void reservationEndingAtClosingTimeIsValid() {
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);
        freeSlotAndSavingWithId();

        assertEquals(LUNES_NUEVE.withHour(12), service.ejecutar(command(LUNES_NUEVE.withHour(11))).getFechaFin());
    }

    @Test
    @DisplayName("Rechaza la reserva si el usuario no es cliente")
    void rejectsNonClient() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(ID_USUARIO)).thenReturn(Optional.empty());

        CrearReservaComando peticion = command(LUNES_NUEVE);
        assertThrows(UsuarioNoEsClienteException.class, () -> service.ejecutar(peticion));
        verifyNoInteractions(obtenerServicioPorIdUseCase, reservaRepositoryPort);
    }

    @Test
    @DisplayName("Rechaza la reserva si el servicio no existe")
    void rejectsUnknownService() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(ID_USUARIO)).thenReturn(Optional.of(ID_CLIENTE));
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenThrow(new ServicioNoEncontradoException(ID_SERVICIO));

        CrearReservaComando peticion = command(LUNES_NUEVE);
        ServicioNoReservableException ex = assertThrows(ServicioNoReservableException.class, () -> service.ejecutar(peticion));

        assertEquals("No se encontró el servicio con ID: 10", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza una reserva en el pasado")
    void rejectsPastReservation() {
        clientAndService();

        CrearReservaComando enElPasado = command(LocalDateTime.of(2026, 10, 1, 9, 0));
        assertThrows(FechaEnElPasadoException.class, () -> service.ejecutar(enElPasado));
        verifyNoInteractions(disponibilidadAgenda);
    }

    @Test
    @DisplayName("Rechaza la reserva si el servicio no tiene agenda activa")
    void rejectsServiceWithoutAgenda() {
        clientAndService();
        when(disponibilidadAgenda.consultarHorariosDisponibles(ID_PROVEEDOR, ID_SERVICIO)).thenThrow(new AgendaNoEncontradaException(10L));

        CrearReservaComando peticion = command(LUNES_NUEVE);
        HorarioNoDisponibleException ex = assertThrows(HorarioNoDisponibleException.class, () -> service.ejecutar(peticion));

        assertEquals(HorarioNoDisponibleException.sinAgenda(ID_SERVICIO).getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza una reserva que no cabe en ningún horario de la agenda")
    void rejectsReservationOutsideAgenda() {
        // Arrange
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE,
                new Horario(2L, DiaSemana.MARTES, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new Horario(3L, DiaSemana.LUNES, LocalTime.of(10, 0), LocalTime.of(23, 0)));
        LocalDateTime antesDeAbrir = LUNES_NUEVE.withHour(7);
        CrearReservaComando peticion = command(antesDeAbrir);

        // Act
        HorarioNoDisponibleException ex = assertThrows(HorarioNoDisponibleException.class, () -> service.ejecutar(peticion));

        // Assert
        assertEquals(HorarioNoDisponibleException.fueraDeAgenda(ID_SERVICIO, antesDeAbrir, antesDeAbrir.plusMinutes(60)).getMessage(),
                ex.getMessage());
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Rechaza una reserva que termina después del cierre o que cruza a otro día")
    void rejectsReservationEndingTooLate() {
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE, new Horario(2L, DiaSemana.LUNES, LocalTime.of(22, 0), LocalTime.of(23, 59)));

        CrearReservaComando terminaTarde = command(LUNES_NUEVE.withHour(11).withMinute(30));
        assertThrows(HorarioNoDisponibleException.class, () -> service.ejecutar(terminaTarde));
        CrearReservaComando cruzaMedianoche = command(LUNES_NUEVE.withHour(23).withMinute(30));
        assertThrows(HorarioNoDisponibleException.class, () -> service.ejecutar(cruzaMedianoche));
    }

    @Test
    @DisplayName("Rechaza una reserva que no empieza en un turno del horario")
    void rejectsReservationOutsideSlot() {
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);

        CrearReservaComando aMediaHora = command(LUNES_NUEVE.withMinute(30));
        HoraFueraDeTurnoException ex = assertThrows(HoraFueraDeTurnoException.class,
                () -> service.ejecutar(aMediaHora));

        assertEquals("Este servicio se reserva cada 60 minutos desde las 08:00.", ex.getMessage());
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Rechaza una reserva que se cruza con otra vigente del servicio")
    void rejectsOverlappingReservation() {
        // Arrange
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);
        Reserva enElBorde = Reserva.reconstruir(1, 9L, ID_SERVICIO, ID_PROVEEDOR, LUNES_NUEVE.minusHours(1), LUNES_NUEVE,
                EstadoReserva.RESERVADO, null, null, null, null);
        Reserva cruzada = Reserva.reconstruir(2, 9L, ID_SERVICIO, ID_PROVEEDOR, LUNES_NUEVE.plusMinutes(30), LUNES_NUEVE.plusMinutes(90),
                EstadoReserva.RESERVADO, null, null, null, null);
        when(reservaRepositoryPort.buscarActivasQueSeSolapan(ID_SERVICIO, LUNES_NUEVE, LUNES_NUEVE.plusMinutes(60)))
                .thenReturn(List.of(enElBorde, cruzada));
        CrearReservaComando peticion = command(LUNES_NUEVE);

        // Act
        HorarioNoDisponibleException ex = assertThrows(HorarioNoDisponibleException.class, () -> service.ejecutar(peticion));

        // Assert
        assertEquals(HorarioNoDisponibleException.yaReservado(ID_SERVICIO, LUNES_NUEVE, LUNES_NUEVE.plusMinutes(60)).getMessage(),
                ex.getMessage());
        verify(reservaRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Un recurso opcional no válido se informa como error de reservas")
    void invalidOptionalResourceBecomesReservationError() {
        // Arrange
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);
        freeSlotAndSavingWithId();
        RecursoOpcionalNoValidoException causa = new RecursoOpcionalNoValidoException("'Masaje'", List.of("'Toalla'"));
        when(apartarRecursosDeReservaUseCase.ejecutar(any(), any(), any(), any(), any())).thenThrow(causa);
        CrearReservaComando peticion = command(LUNES_NUEVE);

        // Act
        RecursoOpcionalInvalidoException ex = assertThrows(RecursoOpcionalInvalidoException.class,
                () -> service.ejecutar(peticion));

        // Assert
        assertEquals(causa.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Un recurso que no se puede apartar se informa como recurso no disponible")
    void unavailableResourceBecomesReservationError() {
        // Arrange
        clientAndService();
        agendaWith(LUNES_DE_OCHO_A_DOCE);
        freeSlotAndSavingWithId();
        RecursoDesactivadoException causa = RecursoDesactivadoException.alReservar("Aceite");
        when(apartarRecursosDeReservaUseCase.ejecutar(any(), any(), any(), any(), any())).thenThrow(causa);
        CrearReservaComando peticion = command(LUNES_NUEVE);

        // Act
        RecursoNoDisponibleException ex = assertThrows(RecursoNoDisponibleException.class,
                () -> service.ejecutar(peticion));

        // Assert
        assertEquals(causa.getMessage(), ex.getMessage());
    }
}
