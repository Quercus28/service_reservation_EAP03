package com.service_reservation.EAP03.modulos.reportes.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReporteOcupacionTest {

    @Test
    @DisplayName("Garantiza la inmutabilidad de la lista de detalle de servicios")
    void listaDetalleEsInmutable() {
        PeriodoReporte periodo = new PeriodoReporte(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        OcupacionServicio s1 = OcupacionServicio.builder(1, "Servicio 1").build();
        List<OcupacionServicio> listaOriginal = new ArrayList<>();
        listaOriginal.add(s1);

        ReporteOcupacion reporte = ReporteOcupacion.builder(10, periodo)
                .detalleServicios(listaOriginal)
                .build();

        List<OcupacionServicio> detalle = reporte.getDetalleServicios();

        OcupacionServicio otroServicio = OcupacionServicio.builder(2, "Servicio 2").build();

        assertThrows(UnsupportedOperationException.class, () -> detalle.add(otroServicio));
    }

    @Test
    @DisplayName("Requiere idProveedor y PeriodoReporte de forma obligatoria")
    void requiereCamposObligatorios() {
        PeriodoReporte periodo = new PeriodoReporte(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThrows(NullPointerException.class, () -> ReporteOcupacion.builder(null, periodo).build());
        assertThrows(NullPointerException.class, () -> ReporteOcupacion.builder(1, null).build());
    }
}
