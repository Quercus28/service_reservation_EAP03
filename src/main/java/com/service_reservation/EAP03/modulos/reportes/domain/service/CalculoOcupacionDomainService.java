package com.service_reservation.EAP03.modulos.reportes.domain.service;

import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;

import java.util.List;

/**
 * Domain Service: encapsula el cálculo matemático de ocupación y cancelaciones.
 *
 * Responsabilidades:
 *  - Calcular el porcentaje de ocupación por servicio y global:
 *      (minutosOcupados / minutosDisponibles) * 100
 *  - Calcular el porcentaje de cancelación por servicio y global:
 *      (citasCanceladas / (totalCitasEfectivas + citasCanceladas)) * 100
 *  - Garantizar 0.0% cuando no hay citas o la capacidad es cero,
 *    evitando ArithmeticException / NaN por división entre cero.
 *
 * Dominio puro: sin anotaciones de Spring ni JPA.
 */
public class CalculoOcupacionDomainService {

    private static final double MINUTOS_EN_HORA = 60.0;

    /**
     * Construye el {@link ReporteOcupacion} a partir de los datos crudos
     * entregados por la capa de infraestructura.
     *
     * @param idProveedor identificador del proveedor autenticado
     * @param periodo     rango de fechas ya validado
     * @param datos       lista de métricas brutas por servicio
     * @return reporte completo con detalle individual y cifras globales
     */
    public ReporteOcupacion calcular(Integer idProveedor, PeriodoReporte periodo,
                                     List<DatosOcupacionServicio> datos) {

        List<OcupacionServicio> detalle = datos.stream()
                .map(this::calcularPorServicio)
                .toList();

        return construirReporteGlobal(idProveedor, periodo, detalle);
    }

    // ── Cálculo individual ───────────────────────────────────────────────────

    private OcupacionServicio calcularPorServicio(DatosOcupacionServicio datos) {
        double horasOcupadas     = minutosAHoras(datos.getMinutosOcupados());
        double horasDisponibles  = minutosAHoras(datos.getMinutosDisponibles());
        double pctOcupacion      = calcularPorcentaje(datos.getMinutosOcupados(), datos.getMinutosDisponibles());
        double pctCancelacion    = calcularPorcentajeCancelacion(datos.getTotalCanceladas(), datos.getTotalCitas());

        return OcupacionServicio.builder(datos.getIdServicio(), datos.getNombreServicio())
                .totalCitas(datos.getTotalCitas())
                .horasOcupadas(redondear(horasOcupadas))
                .horasDisponibles(redondear(horasDisponibles))
                .porcentajeOcupacion(redondear(pctOcupacion))
                .citasCanceladas(datos.getTotalCanceladas())
                .porcentajeCancelacion(redondear(pctCancelacion))
                .build();
    }

    // ── Cifras globales ──────────────────────────────────────────────────────

    private ReporteOcupacion construirReporteGlobal(Integer idProveedor, PeriodoReporte periodo,
                                                     List<OcupacionServicio> detalle) {

        int    totalCitas         = detalle.stream().mapToInt(OcupacionServicio::getTotalCitas).sum();
        int    totalCanceladas    = detalle.stream().mapToInt(OcupacionServicio::getCitasCanceladas).sum();
        double horasOcupadas      = detalle.stream().mapToDouble(OcupacionServicio::getHorasOcupadas).sum();
        double horasDisponibles   = detalle.stream().mapToDouble(OcupacionServicio::getHorasDisponibles).sum();

        double porcentajeOcupacionGlobal = horasDisponibles == 0.0
                ? 0.0
                : redondear((horasOcupadas / horasDisponibles) * 100.0);

        double porcentajeCancelacionGlobal = calcularPorcentajeCancelacion(totalCanceladas, totalCitas);

        return ReporteOcupacion.builder(idProveedor, periodo)
                .totalCitasGlobal(totalCitas)
                .horasOcupadasGlobal(redondear(horasOcupadas))
                .horasDisponiblesGlobal(redondear(horasDisponibles))
                .porcentajeOcupacionGlobal(porcentajeOcupacionGlobal)
                .totalCanceladasGlobal(totalCanceladas)
                .porcentajeCancelacionGlobal(redondear(porcentajeCancelacionGlobal))
                .detalleServicios(detalle)
                .build();
    }

    // ── Utilitarios matemáticos ──────────────────────────────────────────────

    /**
     * Calcula el porcentaje de ocupación sin división por cero.
     */
    private double calcularPorcentaje(long minutosOcupados, long minutosDisponibles) {
        if (minutosDisponibles <= 0) {
            return 0.0;
        }
        return ((double) minutosOcupados / minutosDisponibles) * 100.0;
    }

    /**
     * Calcula el porcentaje de cancelación frente al total de reservas solicitadas
     * (reservas efectivas + reservas canceladas).
     */
    private double calcularPorcentajeCancelacion(int canceladas, int efectivas) {
        int totalSolicitadas = efectivas + canceladas;
        if (totalSolicitadas <= 0) {
            return 0.0;
        }
        return ((double) canceladas / totalSolicitadas) * 100.0;
    }

    private double minutosAHoras(long minutos) {
        return minutos / MINUTOS_EN_HORA;
    }

    /** Redondea a dos decimales para presentar cifras limpias en la respuesta. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
