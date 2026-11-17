package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.Motivo;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida con lista blanca los parámetros de la consulta del historial. Cualquier nombre fuera de la
 * lista, valor fuera de formato o texto con aspecto de comando se rechaza con un error genérico.
 */
public record ConsultaHistorialParametros(Long idCliente, EstadoReserva estado, int page, int size) {

    public static final int SIZE_POR_DEFECTO = 10;
    public static final int SIZE_MAXIMO = 50;

    private static final Set<String> PERMITIDOS = Set.of("idCliente", "estado", "page", "size");
    // Comillas, punto y coma, comentarios SQL, etiquetas, barra invertida y la palabra "script".
    private static final Pattern SOSPECHOSO = Pattern.compile("['\";<>\\\\]|--|/\\*|script", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENTERO = Pattern.compile("\\d{1,9}");

    public static ConsultaHistorialParametros desde(Map<String, String[]> parametros) {
        // 1) Texto sospechoso en nombres o valores.
        for (Map.Entry<String, String[]> e : parametros.entrySet()) {
            if (SOSPECHOSO.matcher(e.getKey()).find()) {
                throw new ConsultaHistorialInvalidaException(Motivo.PATRON_SOSPECHOSO);
            }
            for (String v : e.getValue()) {
                if (v != null && SOSPECHOSO.matcher(v).find()) {
                    throw new ConsultaHistorialInvalidaException(Motivo.PATRON_SOSPECHOSO);
                }
            }
        }
        // 2) Solo los parámetros conocidos, una vez cada uno.
        for (Map.Entry<String, String[]> e : parametros.entrySet()) {
            if (!PERMITIDOS.contains(e.getKey())) {
                throw new ConsultaHistorialInvalidaException(Motivo.PARAMETRO_DESCONOCIDO);
            }
            if (e.getValue().length != 1) {
                throw new ConsultaHistorialInvalidaException(Motivo.VALOR_INVALIDO);
            }
        }
        // 3) Formato y rango de cada valor.
        String idCliente = unico(parametros, "idCliente");
        String estado = unico(parametros, "estado");
        String page = unico(parametros, "page");
        String size = unico(parametros, "size");

        int pageNum = page == null ? 0 : entero(page);
        int sizeNum = size == null ? SIZE_POR_DEFECTO : entero(size);
        if (sizeNum < 1 || sizeNum > SIZE_MAXIMO) {
            throw new ConsultaHistorialInvalidaException(Motivo.VALOR_INVALIDO);
        }
        Long idClienteNum = null;
        if (idCliente != null) {
            if (!ENTERO.matcher(idCliente).matches()) {
                throw new ConsultaHistorialInvalidaException(Motivo.VALOR_INVALIDO);
            }
            idClienteNum = Long.parseLong(idCliente);
        }
        return new ConsultaHistorialParametros(idClienteNum, estadoDe(estado), pageNum, sizeNum);
    }

    private static String unico(Map<String, String[]> parametros, String nombre) {
        String[] valores = parametros.get(nombre);
        return valores == null ? null : valores[0];
    }

    private static int entero(String texto) {
        if (!ENTERO.matcher(texto).matches()) {
            throw new ConsultaHistorialInvalidaException(Motivo.VALOR_INVALIDO);
        }
        return Integer.parseInt(texto);
    }

    private static EstadoReserva estadoDe(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return EstadoReserva.valueOf(texto);
        } catch (IllegalArgumentException e) {
            throw new ConsultaHistorialInvalidaException(Motivo.VALOR_INVALIDO);
        }
    }
}