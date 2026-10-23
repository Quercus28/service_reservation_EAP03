package com.service_reservation.EAP03.modulos.availability.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.ServicioDisponibilidadInfo;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ConsultarReservasActivasUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityJpaAdapterTest {

    private static final Integer ID_SERVICIO = 10;
    private static final Integer ID_PROVEEDOR = 7;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 11, 3, 23, 59);

    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    @Mock private IDisponibilidad disponibilidadAgendasPort;
    @Mock private ConsultarReservasActivasUseCase consultarReservasActivasUseCase;

    @InjectMocks private AvailabilityJpaAdapter adapter;

    // ---- obtenerServicioInfo ----

    @Test
    @DisplayName("obtenerServicioInfo mapea el servicio del catálogo")
    void obtenerServicioInfoMapea() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO))
                .thenReturn(new ServicioResponse(ID_SERVICIO, ID_PROVEEDOR, "Masaje", 45, null));

        Optional<ServicioDisponibilidadInfo> info = adapter.obtenerServicioInfo(ID_SERVICIO);

        assertEquals(Optional.of(new ServicioDisponibilidadInfo(ID_SERVICIO, ID_PROVEEDOR, 45)), info);
    }

    @Test
    @DisplayName("obtenerServicioInfo con id nulo no consulta el catálogo")
    void obtenerServicioInfoIdNulo() {
        assertTrue(adapter.obtenerServicioInfo(null).isEmpty());
        verifyNoInteractions(obtenerServicioPorIdUseCase);
    }

    @Test
    @DisplayName("obtenerServicioInfo devuelve vacío si el catálogo responde null")
    void obtenerServicioInfoRespuestaNula() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(null);

        assertTrue(adapter.obtenerServicioInfo(ID_SERVICIO).isEmpty());
    }

    @Test
    @DisplayName("obtenerServicioInfo devuelve vacío si el servicio no existe")
    void obtenerServicioInfoNoEncontrado() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenThrow(new ServicioNoEncontradoException(ID_SERVICIO));

        assertTrue(adapter.obtenerServicioInfo(ID_SERVICIO).isEmpty());
    }

    // ---- obtenerHorariosProveedor ----

    @Test
    @DisplayName("obtenerHorariosProveedor mapea los horarios de la agenda")
    void obtenerHorariosMapea() {
        when(disponibilidadAgendasPort.consultarHorariosDisponibles(ID_PROVEEDOR, ID_SERVICIO)).thenReturn(List.of(
                new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new Horario(2L, DiaSemana.JUEVES, LocalTime.of(14, 0), LocalTime.of(18, 0))));

        List<HorarioSemanal> horarios = adapter.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR);

        assertEquals(List.of(
                new HorarioSemanal(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new HorarioSemanal(DiaSemana.JUEVES, LocalTime.of(14, 0), LocalTime.of(18, 0))), horarios);
    }

    @Test
    @DisplayName("obtenerHorariosProveedor con ids nulos no consulta la agenda")
    void obtenerHorariosIdsNulos() {
        assertTrue(adapter.obtenerHorariosProveedor(null, ID_PROVEEDOR).isEmpty());
        assertTrue(adapter.obtenerHorariosProveedor(ID_SERVICIO, null).isEmpty());
        verifyNoInteractions(disponibilidadAgendasPort);
    }

    @Test
    @DisplayName("obtenerHorariosProveedor devuelve vacío si la agenda responde null")
    void obtenerHorariosRespuestaNula() {
        when(disponibilidadAgendasPort.consultarHorariosDisponibles(ID_PROVEEDOR, ID_SERVICIO)).thenReturn(null);

        assertTrue(adapter.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR).isEmpty());
    }

    @Test
    @DisplayName("obtenerHorariosProveedor devuelve vacío si no hay agenda")
    void obtenerHorariosSinAgenda() {
        when(disponibilidadAgendasPort.consultarHorariosDisponibles(ID_PROVEEDOR, ID_SERVICIO))
                .thenThrow(new AgendaNoEncontradaException(1L));

        assertTrue(adapter.obtenerHorariosProveedor(ID_SERVICIO, ID_PROVEEDOR).isEmpty());
    }

    // ---- obtenerReservasActivas ----

    @Test
    @DisplayName("obtenerReservasActivas convierte los intervalos reservados en franjas")
    void obtenerReservasMapea() {
        LocalDateTime ocho = INICIO.withHour(8);
        when(consultarReservasActivasUseCase.ejecutar(ID_SERVICIO, INICIO, FIN))
                .thenReturn(List.of(new IntervaloReservado(ocho, ocho.plusHours(1))));

        List<TimeSlot> reservas = adapter.obtenerReservasActivas(ID_SERVICIO, ID_PROVEEDOR, INICIO, FIN);

        assertEquals(List.of(new TimeSlot(ocho, ocho.plusHours(1))), reservas);
    }

    @Test
    @DisplayName("obtenerReservasActivas no requiere el id del proveedor")
    void obtenerReservasSinProveedor() {
        when(consultarReservasActivasUseCase.ejecutar(ID_SERVICIO, INICIO, FIN)).thenReturn(List.of());

        assertTrue(adapter.obtenerReservasActivas(ID_SERVICIO, null, INICIO, FIN).isEmpty());
        verify(consultarReservasActivasUseCase).ejecutar(ID_SERVICIO, INICIO, FIN);
    }

    @Test
    @DisplayName("obtenerReservasActivas con parámetros nulos no consulta reservas")
    void obtenerReservasParametrosNulos() {
        assertTrue(adapter.obtenerReservasActivas(null, ID_PROVEEDOR, INICIO, FIN).isEmpty());
        assertTrue(adapter.obtenerReservasActivas(ID_SERVICIO, ID_PROVEEDOR, null, FIN).isEmpty());
        assertTrue(adapter.obtenerReservasActivas(ID_SERVICIO, ID_PROVEEDOR, INICIO, null).isEmpty());
        verifyNoInteractions(consultarReservasActivasUseCase);
    }

    @Test
    @DisplayName("obtenerReservasActivas devuelve vacío si reservas responde null")
    void obtenerReservasRespuestaNula() {
        when(consultarReservasActivasUseCase.ejecutar(ID_SERVICIO, INICIO, FIN)).thenReturn(null);

        assertTrue(adapter.obtenerReservasActivas(ID_SERVICIO, ID_PROVEEDOR, INICIO, FIN).isEmpty());
    }
}
