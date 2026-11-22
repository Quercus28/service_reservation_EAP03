package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.projection.ResumenReservasPorServicioProjection;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.repository.ReporteReservaRepository;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ConsultarServiciosUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcupacionJpaAdapterTest {

    private static final Integer PROVEEDOR_ID = 7;
    // Del lunes 2 al domingo 8 de noviembre de 2026: cada día de la semana aparece una sola vez.
    private static final PeriodoReporte PERIODO =
            new PeriodoReporte(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 8));
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 0, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 11, 9, 0, 0);

    @Mock private ConsultarServiciosUseCase consultarServiciosUseCase;
    @Mock private IDisponibilidad disponibilidadAgendaPort;
    @Mock private ReporteReservaRepository reservaRepository;

    @InjectMocks private OcupacionJpaAdapter adapter;

    private static ServicioResponse servicio(int id, String nombre, int duracion) {
        return new ServicioResponse(id, PROVEEDOR_ID, nombre, duracion, null);
    }

    private static PaginaResponse<ServicioResponse> pagina(List<ServicioResponse> contenido) {
        return new PaginaResponse<>(contenido, 0, 1000, 0, 0, true, true);
    }

    private static ResumenReservasPorServicioProjection resumen(int idServicio, long total) {
        return new ResumenReservasPorServicioProjection() {
            @Override public Integer getIdServicio() { return idServicio; }
            @Override public Long getTotalCitas() { return total; }
        };
    }

    @Test
    @DisplayName("Si el catálogo no devuelve página, no hay datos y no se consultan reservas")
    void sinPagina() {
        when(consultarServiciosUseCase.ejecutar(PROVEEDOR_ID, null, 0, 1000)).thenReturn(null);

        assertEquals(List.of(), adapter.obtenerDatosOcupacion(PROVEEDOR_ID, PERIODO));
        verifyNoInteractions(reservaRepository, disponibilidadAgendaPort);
    }

    @Test
    @DisplayName("Si la página no trae contenido, no hay datos y no se consultan reservas")
    void paginaSinContenido() {
        when(consultarServiciosUseCase.ejecutar(PROVEEDOR_ID, null, 0, 1000)).thenReturn(pagina(null));

        assertEquals(List.of(), adapter.obtenerDatosOcupacion(PROVEEDOR_ID, PERIODO));
        verifyNoInteractions(reservaRepository, disponibilidadAgendaPort);
    }

    @Test
    @DisplayName("Si el proveedor no tiene servicios, no hay datos y no se consultan reservas")
    void proveedorSinServicios() {
        when(consultarServiciosUseCase.ejecutar(PROVEEDOR_ID, null, 0, 1000)).thenReturn(pagina(List.of()));

        assertEquals(List.of(), adapter.obtenerDatosOcupacion(PROVEEDOR_ID, PERIODO));
        verifyNoInteractions(reservaRepository, disponibilidadAgendaPort);
    }

    @Test
    @DisplayName("Cruza cada servicio con sus citas, cancelaciones y la capacidad de su agenda")
    void cruzaServiciosConReservasYAgenda() {
        when(consultarServiciosUseCase.ejecutar(PROVEEDOR_ID, null, 0, 1000)).thenReturn(pagina(List.of(
                servicio(1, "Corte", 30),
                servicio(2, "Tinte", 60),
                servicio(3, "Peinado", 45))));
        when(reservaRepository.contarCitasPorServicio(PROVEEDOR_ID, EstadoReserva.CANCELADO, INICIO, FIN))
                .thenReturn(List.of(resumen(1, 3)));
        when(reservaRepository.contarCanceladasPorServicio(PROVEEDOR_ID, EstadoReserva.CANCELADO, INICIO, FIN))
                .thenReturn(List.of(resumen(1, 1), resumen(2, 2)));
        when(disponibilidadAgendaPort.consultarHorariosDisponibles(PROVEEDOR_ID, 1))
                .thenReturn(List.of(new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(12, 0))));
        when(disponibilidadAgendaPort.consultarHorariosDisponibles(PROVEEDOR_ID, 2)).thenReturn(null);
        when(disponibilidadAgendaPort.consultarHorariosDisponibles(PROVEEDOR_ID, 3))
                .thenThrow(new AgendaNoEncontradaException(3L));

        List<DatosOcupacionServicio> datos = adapter.obtenerDatosOcupacion(PROVEEDOR_ID, PERIODO);

        assertEquals(3, datos.size());

        DatosOcupacionServicio corte = datos.get(0);
        assertEquals(1, corte.getIdServicio());
        assertEquals("Corte", corte.getNombreServicio());
        assertEquals(30, corte.getDuracionMinutosServicio());
        assertEquals(3, corte.getTotalCitas());
        assertEquals(1, corte.getTotalCanceladas());
        assertEquals(90, corte.getMinutosOcupados());
        assertEquals(180, corte.getMinutosDisponibles());

        // Sin citas efectivas y con la agenda devolviendo null: capacidad 0.
        DatosOcupacionServicio tinte = datos.get(1);
        assertEquals(0, tinte.getTotalCitas());
        assertEquals(2, tinte.getTotalCanceladas());
        assertEquals(0, tinte.getMinutosOcupados());
        assertEquals(0, tinte.getMinutosDisponibles());

        // Sin agenda registrada: el reporte sigue, con capacidad 0.
        DatosOcupacionServicio peinado = datos.get(2);
        assertEquals(0, peinado.getTotalCitas());
        assertEquals(0, peinado.getTotalCanceladas());
        assertEquals(0, peinado.getMinutosDisponibles());
    }
}
