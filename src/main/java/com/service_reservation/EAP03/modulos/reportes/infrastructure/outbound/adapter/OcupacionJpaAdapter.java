package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.repository.AgendaSpringDataRepository;
import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.out.OcupacionDataPort;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.CapacidadAgendaCalculator;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.projection.ResumenReservasPorServicioProjection;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence.repository.ReporteReservaRepository;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence.ServicioJpaEntity;
import com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence.SpringDataServicioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adaptador de salida (Driven Adapter) que implementa {@link OcupacionDataPort}.
 *
 * Estrategia de consulta:
 *  1. Carga los servicios activos del proveedor (tabla: servicio).
 *  2. Ejecuta una única consulta de agregación sobre reservas para obtener
 *     el conteo de citas efectivas por servicio, excluyendo CANCELADO a nivel SQL.
 *  3. Carga las agendas activas del proveedor (con sus horarios) y delega
 *     el cálculo de capacidad al helper {@link CapacidadAgendaCalculator}.
 *  4. Cruza los tres conjuntos de datos en memoria (N de servicios típicamente
 *     pequeño: single-digit a few dozen) y construye la lista de
 *     {@link DatosOcupacionServicio} para el dominio.
 *
 * Los servicios sin reservas en el período se incluyen con totalCitas = 0
 * y minutosOcupados = 0, garantizando el caso borde de período vacío.
 */
@Component
public class OcupacionJpaAdapter implements OcupacionDataPort {

    private final SpringDataServicioRepository servicioRepository;
    private final ReporteReservaRepository     reservaRepository;
    private final AgendaSpringDataRepository   agendaRepository;

    public OcupacionJpaAdapter(SpringDataServicioRepository servicioRepository,
                               ReporteReservaRepository reservaRepository,
                               AgendaSpringDataRepository agendaRepository) {
        this.servicioRepository = servicioRepository;
        this.reservaRepository  = reservaRepository;
        this.agendaRepository   = agendaRepository;
    }

    @Override
    public List<DatosOcupacionServicio> obtenerDatosOcupacion(Integer proveedorId,
                                                               PeriodoReporte periodo) {
        // 1. Servicios del proveedor
        List<ServicioJpaEntity> servicios = servicioRepository.findByIdProveedor(proveedorId);
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

        // 3. Capacidad: agendas activas del proveedor (con horarios cargados)
        Map<Integer, AgendaJpaEntity> agendaPorServicio =
                agendaRepository.findAll().stream()
                        .filter(a -> proveedorId.equals(a.getIdProveedor()) && a.isActiva())
                        .collect(Collectors.toMap(
                                AgendaJpaEntity::getIdServicio,
                                Function.identity(),
                                // Si existieran duplicados (no debería por unique constraint), toma la primera
                                (a1, a2) -> a1
                        ));

        // 4. Cruzar y construir DatosOcupacionServicio
        return servicios.stream()
                .map(servicio -> construirDatos(servicio, citasPorServicio, agendaPorServicio, periodo))
                .toList();
    }

    private DatosOcupacionServicio construirDatos(ServicioJpaEntity servicio,
                                                   Map<Integer, Long> citasPorServicio,
                                                   Map<Integer, AgendaJpaEntity> agendaPorServicio,
                                                   PeriodoReporte periodo) {
        int     totalCitas        = citasPorServicio.getOrDefault(servicio.getId(), 0L).intValue();
        long    minutosOcupados   = (long) totalCitas * servicio.getDuracionMinutos();

        AgendaJpaEntity agenda    = agendaPorServicio.get(servicio.getId());
        long minutosDisponibles   = agenda != null
                ? CapacidadAgendaCalculator.calcularMinutosDisponibles(agenda, periodo)
                : 0L;

        return new DatosOcupacionServicio(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDuracionMinutos(),
                totalCitas,
                minutosOcupados,
                minutosDisponibles
        );
    }
}
