package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.ConsultaReporteInvalidaException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.FormatoParametroReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ParametroReporteFaltanteException;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.ReporteAjenoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaOcupacionParametrosTest {

    private static Map<String, String[]> params(String... claveValor) {
        Map<String, String[]> mapa = new LinkedHashMap<>();
        for (int i = 0; i < claveValor.length; i += 2) {
            mapa.put(claveValor[i], new String[]{claveValor[i + 1]});
        }
        return mapa;
    }

    @Test
    @DisplayName("Acepta startDate y endDate en formato YYYY-MM-DD")
    void aceptaFechasValidas() {
        ConsultaOcupacionParametros p = ConsultaOcupacionParametros.desde(
                params("startDate", "2026-11-01", "endDate", "2026-11-30"));

        assertEquals(LocalDate.of(2026, 11, 1), p.startDate());
        assertEquals(LocalDate.of(2026, 11, 30), p.endDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"proveedorId", "idProveedor", "PROVEEDORID", "IdPrOvEeDoR", "proveedor_id",
            "id_proveedor", "providerId", "provider_id", "proveedor"})
    @DisplayName("Un parámetro que nombre a un proveedor, en cualquier mezcla de mayúsculas, es un reporte ajeno")
    void parametroDeProveedorEsReporteAjeno(String nombre) {
        Map<String, String[]> mapa = params("startDate", "2026-11-01", "endDate", "2026-11-30", nombre, "9");

        assertThrows(ReporteAjenoException.class, () -> ConsultaOcupacionParametros.desde(mapa));
    }

    @Test
    @DisplayName("El parámetro de proveedor se detecta aunque además falten las fechas")
    void parametroDeProveedorTienePrioridad() {
        Map<String, String[]> mapa = params("idProveedor", "9");

        assertThrows(ReporteAjenoException.class, () -> ConsultaOcupacionParametros.desde(mapa));
    }

    @Test
    @DisplayName("Un parámetro desconocido se rechaza con mensaje fijo")
    void rechazaParametroDesconocido() {
        Map<String, String[]> mapa = params("startDate", "2026-11-01", "endDate", "2026-11-30", "x", "<script>");

        ConsultaReporteInvalidaException ex = assertThrows(ConsultaReporteInvalidaException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals(ConsultaReporteInvalidaException.Motivo.PARAMETRO_DESCONOCIDO, ex.getMotivo());
        assertEquals("La solicitud de consulta no es válida.", ex.getMessage());
    }

    @Test
    @DisplayName("Un parámetro repetido se rechaza")
    void rechazaParametroRepetido() {
        Map<String, String[]> mapa = params("endDate", "2026-11-30");
        mapa.put("startDate", new String[]{"2026-11-01", "2026-01-01"});

        ConsultaReporteInvalidaException ex = assertThrows(ConsultaReporteInvalidaException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals(ConsultaReporteInvalidaException.Motivo.PARAMETRO_REPETIDO, ex.getMotivo());
    }

    @Test
    @DisplayName("Falta startDate: el mensaje nombra el campo")
    void faltaStartDate() {
        Map<String, String[]> mapa = params("endDate", "2026-11-30");

        ParametroReporteFaltanteException ex = assertThrows(ParametroReporteFaltanteException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals("El parámetro 'startDate' es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("Faltan las dos fechas: se nombra primero startDate")
    void faltanAmbas() {
        Map<String, String[]> mapa = Map.of();

        ParametroReporteFaltanteException ex = assertThrows(ParametroReporteFaltanteException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals("El parámetro 'startDate' es obligatorio", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("endDate vacío o solo espacios cuenta como faltante")
    void endDateVacio(String valor) {
        Map<String, String[]> mapa = params("startDate", "2026-11-01", "endDate", valor);

        ParametroReporteFaltanteException ex = assertThrows(ParametroReporteFaltanteException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals("El parámetro 'endDate' es obligatorio", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13-45", "02/11/2026", "2026-02-30", "2026-11-02' OR '1'='1", "abc"})
    @DisplayName("Una fecha mal escrita o que no existe es un error de formato que nombra el campo")
    void fechaMalEscrita(String valor) {
        Map<String, String[]> mapa = params("startDate", valor, "endDate", "2026-11-30");

        FormatoParametroReporteInvalidoException ex = assertThrows(FormatoParametroReporteInvalidoException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals("El parámetro 'startDate' tiene un formato o valor inválido", ex.getMessage());
    }

    @Test
    @DisplayName("Un endDate sin valor (null) cuenta como faltante")
    void endDateNulo() {
        Map<String, String[]> mapa = params("startDate", "2026-11-01", "endDate", null);

        ParametroReporteFaltanteException ex = assertThrows(ParametroReporteFaltanteException.class,
                () -> ConsultaOcupacionParametros.desde(mapa));

        assertEquals("El parámetro 'endDate' es obligatorio", ex.getMessage());
    }
}
