package com.service_reservation.EAP03.modulos.reportes.domain.exception;

/**
 * La petición del reporte nombra a un proveedor (por ejemplo ?proveedorId=7). El reporte siempre
 * es del proveedor autenticado, así que se rechaza como intento de ver el reporte de otro.
 */
public class ReporteAjenoException extends RuntimeException {

    public ReporteAjenoException() {
        super("No está permitido especificar el identificador del proveedor en la petición");
    }
}
