package com.service_reservation.EAP03.modulos.reportes.domain.ports.out;

import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;

import java.util.List;

/**
 * Puerto de salida (Driven Port) del módulo de reportes.
 * Define el contrato que el adaptador de infraestructura debe satisfacer
 * para proveer los datos crudos de ocupación al dominio.
 *
 * La implementación (PASO 3) consulta reservas y agendas aplicando
 * todas las exclusiones de negocio (sin estado CANCELADO, solo el proveedor).
 */
public interface OcupacionDataPort {

    /**
     * Retorna los datos de ocupación agrupados por servicio para el
     * proveedor y período indicados, excluyendo reservas CANCELADAS.
     *
     * @param proveedorId identificador del proveedor (extraído del JWT)
     * @param periodo     rango de fechas ya validado por el dominio
     * @return lista con métricas crudas por servicio; vacía si no hay datos
     */
    List<DatosOcupacionServicio> obtenerDatosOcupacion(Integer proveedorId, PeriodoReporte periodo);
}
