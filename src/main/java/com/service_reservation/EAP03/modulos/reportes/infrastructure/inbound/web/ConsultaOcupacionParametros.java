package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.ConsultaReporteInvalidaException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ConsultaReporteInvalidaException.Motivo;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.FormatoParametroReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ParametroReporteFaltanteException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ReporteAjenoException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Valida con lista blanca los parámetros del reporte de ocupación: solo startDate y endDate,
 * una vez cada uno, en formato YYYY-MM-DD. Las reglas del período (fin antes que inicio,
 * más de 366 días) las revisa PeriodoReporte en el dominio.
 *
 * El orden importa: primero se busca un parámetro que nombre a un proveedor (es un intento de
 * ver el reporte de otro y se audita), después los parámetros desconocidos o repetidos, después
 * las fechas que faltan y al final su formato.
 */
public record ConsultaOcupacionParametros(LocalDate startDate, LocalDate endDate) {

    static final String START_DATE = "startDate";
    static final String END_DATE = "endDate";

    private static final Set<String> PERMITIDOS = Set.of(START_DATE, END_DATE);

    public static ConsultaOcupacionParametros desde(Map<String, String[]> parametros) {
        // 1) Un parámetro que nombre a un proveedor, escrito como sea: proveedorId, IDPROVEEDOR,
        //    proveedor_id, providerId...
        for (String nombre : parametros.keySet()) {
            if (nombraProveedor(nombre)) {
                throw new ReporteAjenoException();
            }
        }
        // 2) Solo los parámetros conocidos, una vez cada uno.
        for (Map.Entry<String, String[]> e : parametros.entrySet()) {
            if (!PERMITIDOS.contains(e.getKey())) {
                throw new ConsultaReporteInvalidaException(Motivo.PARAMETRO_DESCONOCIDO);
            }
            if (e.getValue().length != 1) {
                throw new ConsultaReporteInvalidaException(Motivo.PARAMETRO_REPETIDO);
            }
        }
        // 3) Las dos fechas son obligatorias y 4) deben tener formato YYYY-MM-DD.
        String inicio = obligatorio(parametros, START_DATE);
        String fin = obligatorio(parametros, END_DATE);
        return new ConsultaOcupacionParametros(fecha(inicio, START_DATE), fecha(fin, END_DATE));
    }

    private static boolean nombraProveedor(String nombre) {
        String normalizado = nombre.toLowerCase(Locale.ROOT);
        return normalizado.contains("proveedor") || normalizado.contains("provider");
    }

    private static String obligatorio(Map<String, String[]> parametros, String nombre) {
        String[] valores = parametros.get(nombre);
        if (valores == null || valores[0] == null || valores[0].isBlank()) {
            throw new ParametroReporteFaltanteException(nombre);
        }
        return valores[0].trim();
    }

    private static LocalDate fecha(String texto, String nombre) {
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new FormatoParametroReporteInvalidoException(nombre);
        }
    }
}
