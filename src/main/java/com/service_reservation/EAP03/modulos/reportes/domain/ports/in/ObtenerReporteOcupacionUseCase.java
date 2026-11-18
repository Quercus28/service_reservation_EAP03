package com.service_reservation.EAP03.modulos.reportes.domain.ports.in;

import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;

/**
 * Puerto de entrada (Driving Port) del módulo de reportes.
 * El controlador REST lo invoca con un comando ya construido
 * a partir del contexto de seguridad y los parámetros de la petición.
 */
public interface ObtenerReporteOcupacionUseCase {

    /**
     * Genera el reporte de ocupación para el proveedor autenticado
     * en el período indicado por el comando.
     *
     * @param comando contiene proveedorId (del JWT), startDate y endDate
     * @return reporte con cifras globales y desglose por servicio
     * @throws com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException
     *         si endDate es anterior a startDate (400 Bad Request)
     */
    ReporteOcupacion ejecutar(ConsultaOcupacionComando comando);
}
