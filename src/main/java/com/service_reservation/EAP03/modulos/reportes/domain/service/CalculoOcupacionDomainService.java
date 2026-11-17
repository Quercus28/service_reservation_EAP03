package com.service_reservation.EAP03.modulos.reportes.domain.service;

import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;

import java.util.List;

/**
 * Domain Service: encapsula el cálculo matemático de ocupación.
 *
 * Responsabilidades:
 *  - Calcular el porcentaje de ocupación por servicio:
 *      (minutosOcupados / minutosDisponibles) * 100
 *  - Agregar las cifras globales del proveedor.
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
        double horasOcupadas     = minutosAHoras(datos.minutosOcupados());
        double horasDisponibles  = minutosAHoras(datos.minutosDisponibles());
        double porcentaje        = calcularPorcentaje(datos.minutosOcupados(), datos.minutosDisponibles());

        return new OcupacionServicio(
                datos.idServicio(),
                datos.nombreServicio(),
                datos.totalCitas(),
                redondear(horasOcupadas),
                redondear(horasDisponibles),
                redondear(porcentaje)
        );
    }

    // ── Cifras globales ──────────────────────────────────────────────────────

    private ReporteOcupacion construirReporteGlobal(Integer idProveedor, PeriodoReporte periodo,
                                                     List<OcupacionServicio> detalle) {

        int    totalCitas         = detalle.stream().mapToInt(OcupacionServicio::totalCitas).sum();
        double horasOcupadas      = detalle.stream().mapToDouble(OcupacionServicio::horasOcupadas).sum();
        double horasDisponibles   = detalle.stream().mapToDouble(OcupacionServicio::horasDisponibles).sum();

        // Recalcular globalmente desde horas (ya redondeadas) para mantener coherencia
        double porcentajeGlobal = horasDisponibles == 0.0
                ? 0.0
                : redondear((horasOcupadas / horasDisponibles) * 100.0);

        return ReporteOcupacion.builder(idProveedor, periodo)
                .totalCitasGlobal(totalCitas)
                .horasOcupadasGlobal(redondear(horasOcupadas))
                .horasDisponiblesGlobal(redondear(horasDisponibles))
                .porcentajeOcupacionGlobal(porcentajeGlobal)
                .detalleServicios(detalle)
                .build();
    }

    // ── Utilitarios matemáticos ──────────────────────────────────────────────

    /**
     * Calcula el porcentaje sin lanzar excepción aritmética.
     * Si la capacidad disponible es cero (caso borde: sin agenda configurada),
     * retorna 0.0 de forma segura.
     */
    private double calcularPorcentaje(long minutosOcupados, long minutosDisponibles) {
        if (minutosDisponibles <= 0) {
            return 0.0;
        }
        return ((double) minutosOcupados / minutosDisponibles) * 100.0;
    }

    private double minutosAHoras(long minutos) {
        return minutos / MINUTOS_EN_HORA;
    }

    /** Redondea a dos decimales para presentar cifras limpias en la respuesta. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
