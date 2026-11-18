package com.service_reservation.EAP03.modulos.reportes.application;

import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.in.ObtenerReporteOcupacionUseCase;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.out.OcupacionDataPort;
import com.service_reservation.EAP03.modulos.reportes.domain.service.CalculoOcupacionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación que orquesta el caso de uso de reporte de ocupación.
 *
 * Flujo:
 *  1. Recibe el comando ya construido por el controlador (proveedorId del JWT).
 *  2. Construye PeriodoReporte, que valida las fechas en el dominio.
 *  3. Delega la consulta de datos crudos al puerto de salida.
 *  4. Delega el cálculo matemático al Domain Service.
 *  5. Retorna el ReporteOcupacion completo.
 *
 * El aislamiento de seguridad se garantiza porque el proveedorId SIEMPRE
 * proviene del JWT (nunca de un parámetro del cliente).
 */
@Service
public class OcupacionReporteService implements ObtenerReporteOcupacionUseCase {

    private static final Logger log = LoggerFactory.getLogger(OcupacionReporteService.class);

    private final OcupacionDataPort ocupacionDataPort;
    // Sin estado: se instancia directamente para mantener el dominio libre de Spring
    private final CalculoOcupacionDomainService calculoDomainService;

    public OcupacionReporteService(OcupacionDataPort ocupacionDataPort) {
        this.ocupacionDataPort    = ocupacionDataPort;
        this.calculoDomainService = new CalculoOcupacionDomainService();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteOcupacion ejecutar(ConsultaOcupacionComando comando) {
        // PeriodoReporte valida internamente que endDate >= startDate.
        // Si no, lanza PeriodoReporteInvalidoException → 400 Bad Request.
        PeriodoReporte periodo = new PeriodoReporte(comando.startDate(), comando.endDate());

        log.info("Generando reporte de ocupación: proveedorId={}, periodo=[{} - {}]",
                comando.proveedorId(), periodo.startDate(), periodo.endDate());

        List<DatosOcupacionServicio> datosCrudos =
                ocupacionDataPort.obtenerDatosOcupacion(comando.proveedorId(), periodo);

        ReporteOcupacion reporte = calculoDomainService.calcular(
                comando.proveedorId(), periodo, datosCrudos);

        log.info("Reporte generado: proveedorId={}, totalCitas={}, ocupacionGlobal={}%",
                comando.proveedorId(),
                reporte.getTotalCitasGlobal(),
                reporte.getPorcentajeOcupacionGlobal());

        return reporte;
    }
}
