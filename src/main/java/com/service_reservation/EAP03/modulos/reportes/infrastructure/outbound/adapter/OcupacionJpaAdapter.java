package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.out.OcupacionDataPort;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.CapacidadAgendaCalculator;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.projection.ResumenReservasPorServicioProjection;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.repository.ReporteReservaRepository;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ConsultarServiciosUseCase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adaptador de salida (Driven Adapter) que implementa {@link OcupacionDataPort}.
 *
 * Comunica con:
 *  - Catálogo: {@link ConsultarServiciosUseCase} (puerto público)
 *  - Agendas: {@link IDisponibilidad} (puerto público)
 *  - Reservas: {@link ReporteReservaRepository} (consultas agregadas SQL efectivas y canceladas)
 */
@Component
public class OcupacionJpaAdapter implements OcupacionDataPort {

    private final ConsultarServiciosUseCase  consultarServiciosUseCase;
    private final IDisponibilidad            disponibilidadAgendaPort;
    private final ReporteReservaRepository   reservaRepository;

    public OcupacionJpaAdapter(ConsultarServiciosUseCase consultarServiciosUseCase,
                               IDisponibilidad disponibilidadAgendaPort,
                               ReporteReservaRepository reservaRepository) {
        this.consultarServiciosUseCase = consultarServiciosUseCase;
        this.disponibilidadAgendaPort  = disponibilidadAgendaPort;
        this.reservaRepository         = reservaRepository;
    }

    @Override
    public List<DatosOcupacionServicio> obtenerDatosOcupacion(Integer proveedorId,
                                                               PeriodoReporte periodo) {
        // 1. Obtener servicios del proveedor a través del puerto público de catálogo
        PaginaResponse<ServicioResponse> pagina = consultarServiciosUseCase.ejecutar(
                proveedorId, null, 0, 1000);
        List<ServicioResponse> servicios = pagina != null && pagina.contenido() != null
                ? pagina.contenido()
                : List.of();

        if (servicios.isEmpty()) {
            return List.of();
        }

        LocalDateTime inicio = periodo.startDate().atStartOfDay();
        LocalDateTime fin    = periodo.endDate().plusDays(1).atStartOfDay();

        // 2. Resumen de reservas efectivas (excluyendo CANCELADO a nivel DB)
        List<ResumenReservasPorServicioProjection> resumenesEfectivas =
                reservaRepository.contarCitasPorServicio(
                        proveedorId, EstadoReserva.CANCELADO, inicio, fin);

        Map<Integer, Long> citasPorServicio = resumenesEfectivas.stream()
                .collect(Collectors.toMap(
                        ResumenReservasPorServicioProjection::getIdServicio,
                        ResumenReservasPorServicioProjection::getTotalCitas
                ));

        // 3. Resumen de reservas canceladas a nivel DB
        List<ResumenReservasPorServicioProjection> resumenesCanceladas =
                reservaRepository.contarCanceladasPorServicio(
                        proveedorId, EstadoReserva.CANCELADO, inicio, fin);

        Map<Integer, Long> canceladasPorServicio = resumenesCanceladas.stream()
                .collect(Collectors.toMap(
                        ResumenReservasPorServicioProjection::getIdServicio,
                        ResumenReservasPorServicioProjection::getTotalCitas
                ));

        // 4. Cruzar cada servicio con su capacidad (vía puerto de agendas) y sus reservas
        return servicios.stream()
                .map(servicio -> construirDatos(servicio, proveedorId, citasPorServicio, canceladasPorServicio, periodo))
                .toList();
    }

    private DatosOcupacionServicio construirDatos(ServicioResponse servicio,
                                                   Integer proveedorId,
                                                   Map<Integer, Long> citasPorServicio,
                                                   Map<Integer, Long> canceladasPorServicio,
                                                   PeriodoReporte periodo) {
        int  totalCitas      = citasPorServicio.getOrDefault(servicio.id(), 0L).intValue();
        int  totalCanceladas = canceladasPorServicio.getOrDefault(servicio.id(), 0L).intValue();
        long minutosOcupados = (long) totalCitas * servicio.duracionMinutos();

        List<Horario> horarios = obtenerHorariosSeguros(proveedorId, servicio.id());
        long minutosDisponibles = CapacidadAgendaCalculator.calcularMinutosDisponibles(horarios, periodo);

        return DatosOcupacionServicio.builder(servicio.id(), servicio.nombre())
                .duracionMinutosServicio(servicio.duracionMinutos())
                .totalCitas(totalCitas)
                .totalCanceladas(totalCanceladas)
                .minutosOcupados(minutosOcupados)
                .minutosDisponibles(minutosDisponibles)
                .build();
    }

    private List<Horario> obtenerHorariosSeguros(Integer proveedorId, Integer servicioId) {
        try {
            List<Horario> horarios = disponibilidadAgendaPort.consultarHorariosDisponibles(
                    proveedorId, servicioId);
            return horarios != null ? horarios : Collections.emptyList();
        } catch (AgendaNoEncontradaException e) {
            return Collections.emptyList();
        }
    }
}
