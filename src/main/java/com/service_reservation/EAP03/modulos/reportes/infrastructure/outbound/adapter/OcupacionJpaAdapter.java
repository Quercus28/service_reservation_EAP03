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
 * En estricto apego al Monolito Modular y la Arquitectura Hexagonal:
 *  - Se comunica con el módulo de catálogo a través de su puerto público: {@link ConsultarServiciosUseCase}.
 *  - Se comunica con el módulo de agendas a través de su puerto público: {@link IDisponibilidad}.
 *  - Consulta reservas mediante su repositorio optimizado {@link ReporteReservaRepository}
 *    excluyendo citas CANCELADAS a nivel de base de datos.
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

        // 2. Resumen de reservas efectivas (excluyendo CANCELADO a nivel DB)
        LocalDateTime inicio = periodo.startDate().atStartOfDay();
        LocalDateTime fin    = periodo.endDate().plusDays(1).atStartOfDay();

        List<ResumenReservasPorServicioProjection> resumenes =
                reservaRepository.contarCitasPorServicio(
                        proveedorId, EstadoReserva.CANCELADO, inicio, fin);

        Map<Integer, Long> citasPorServicio = resumenes.stream()
                .collect(Collectors.toMap(
                        ResumenReservasPorServicioProjection::getIdServicio,
                        ResumenReservasPorServicioProjection::getTotalCitas
                ));

        // 3. Cruzar cada servicio con su capacidad (vía puerto de agendas) y sus reservas
        return servicios.stream()
                .map(servicio -> construirDatos(servicio, proveedorId, citasPorServicio, periodo))
                .toList();
    }

    private DatosOcupacionServicio construirDatos(ServicioResponse servicio,
                                                   Integer proveedorId,
                                                   Map<Integer, Long> citasPorServicio,
                                                   PeriodoReporte periodo) {
        int  totalCitas      = citasPorServicio.getOrDefault(servicio.id(), 0L).intValue();
        long minutosOcupados = (long) totalCitas * servicio.duracionMinutos();

        List<Horario> horarios = obtenerHorariosSeguros(proveedorId, servicio.id());
        long minutosDisponibles = CapacidadAgendaCalculator.calcularMinutosDisponibles(horarios, periodo);

        return new DatosOcupacionServicio(
                servicio.id(),
                servicio.nombre(),
                servicio.duracionMinutos(),
                totalCitas,
                minutosOcupados,
                minutosDisponibles
        );
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
