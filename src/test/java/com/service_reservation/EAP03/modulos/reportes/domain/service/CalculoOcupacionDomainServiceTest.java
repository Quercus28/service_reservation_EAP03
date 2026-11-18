package com.service_reservation.EAP03.modulos.reportes.domain.service;

import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.OcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculoOcupacionDomainServiceTest {

    private CalculoOcupacionDomainService service;
    private PeriodoReporte periodo;

    @BeforeEach
    void setUp() {
        service = new CalculoOcupacionDomainService();
        periodo = new PeriodoReporte(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    @Test
    @DisplayName("Calcula ocupación y cancelaciones individuales y globales con cifras normales")
    void calcularMetricasNormales() {
        // Servicio 1: 10 citas efectivas (600 min), 2 canceladas, 1800 min disponibles (30h)
        DatosOcupacionServicio s1 = DatosOcupacionServicio.builder(1, "Corte de Cabello")
                .duracionMinutosServicio(60)
                .totalCitas(10)
                .totalCanceladas(2)
                .minutosOcupados(600)
                .minutosDisponibles(1800)
                .build();

        // Servicio 2: 5 citas efectivas (150 min), 0 canceladas, 600 min disponibles (10h)
        DatosOcupacionServicio s2 = DatosOcupacionServicio.builder(2, "Lavado")
                .duracionMinutosServicio(30)
                .totalCitas(5)
                .totalCanceladas(0)
                .minutosOcupados(150)
                .minutosDisponibles(600)
                .build();

        ReporteOcupacion reporte = service.calcular(100, periodo, List.of(s1, s2));

        // Globales
        assertEquals(100, reporte.getIdProveedor());
        assertEquals(periodo, reporte.getPeriodo());
        assertEquals(15, reporte.getTotalCitasGlobal());
        assertEquals(2, reporte.getTotalCanceladasGlobal());
        assertEquals(12.5, reporte.getHorasOcupadasGlobal()); // 10h + 2.5h
        assertEquals(40.0, reporte.getHorasDisponiblesGlobal()); // 30h + 10h
        // Ocupación global: (12.5 / 40.0) * 100 = 31.25%
        assertEquals(31.25, reporte.getPorcentajeOcupacionGlobal());
        // Cancelación global: 2 canceladas / 17 solicitadas = 11.76%
        assertEquals(11.76, reporte.getPorcentajeCancelacionGlobal());

        // Desglose individual
        assertEquals(2, reporte.getDetalleServicios().size());

        OcupacionServicio os1 = reporte.getDetalleServicios().get(0);
        assertEquals(10, os1.getTotalCitas());
        assertEquals(2, os1.getCitasCanceladas());
        assertEquals(10.0, os1.getHorasOcupadas());
        assertEquals(30.0, os1.getHorasDisponibles());
        assertEquals(33.33, os1.getPorcentajeOcupacion()); // 10/30 = 33.33%
        assertEquals(16.67, os1.getPorcentajeCancelacion()); // 2/12 = 16.67%

        OcupacionServicio os2 = reporte.getDetalleServicios().get(1);
        assertEquals(5, os2.getTotalCitas());
        assertEquals(0, os2.getCitasCanceladas());
        assertEquals(2.5, os2.getHorasOcupadas());
        assertEquals(10.0, os2.getHorasDisponibles());
        assertEquals(25.0, os2.getPorcentajeOcupacion()); // 2.5/10 = 25.0%
        assertEquals(0.0, os2.getPorcentajeCancelacion());
    }

    @Test
    @DisplayName("Caso borde: sin citas registradas retorna 0.0% sin división por cero ni NaN")
    void casoBordeSinCitas() {
        DatosOcupacionServicio s1 = DatosOcupacionServicio.builder(1, "Corte")
                .duracionMinutosServicio(60)
                .totalCitas(0)
                .totalCanceladas(0)
                .minutosOcupados(0)
                .minutosDisponibles(1200)
                .build();

        ReporteOcupacion reporte = service.calcular(100, periodo, List.of(s1));

        assertEquals(0, reporte.getTotalCitasGlobal());
        assertEquals(0, reporte.getTotalCanceladasGlobal());
        assertEquals(0.0, reporte.getHorasOcupadasGlobal());
        assertEquals(20.0, reporte.getHorasDisponiblesGlobal());
        assertEquals(0.0, reporte.getPorcentajeOcupacionGlobal());
        assertEquals(0.0, reporte.getPorcentajeCancelacionGlobal());
    }

    @Test
    @DisplayName("Caso borde: sin capacidad disponible (0 horas) retorna 0.0% de ocupación")
    void casoBordeSinCapacidad() {
        DatosOcupacionServicio s1 = DatosOcupacionServicio.builder(1, "Servicio sin horario")
                .duracionMinutosServicio(60)
                .totalCitas(0)
                .totalCanceladas(0)
                .minutosOcupados(0)
                .minutosDisponibles(0)
                .build();

        ReporteOcupacion reporte = service.calcular(100, periodo, List.of(s1));

        assertEquals(0.0, reporte.getPorcentajeOcupacionGlobal());
        assertFalse(Double.isNaN(reporte.getPorcentajeOcupacionGlobal()));
        assertFalse(Double.isInfinite(reporte.getPorcentajeOcupacionGlobal()));
    }

    @Test
    @DisplayName("Todas las citas canceladas resultan en 100% de cancelación y 0% de ocupación")
    void todasCanceladas() {
        DatosOcupacionServicio s1 = DatosOcupacionServicio.builder(1, "Servicio cancelado")
                .duracionMinutosServicio(60)
                .totalCitas(0)
                .totalCanceladas(3)
                .minutosOcupados(0)
                .minutosDisponibles(1800)
                .build();

        ReporteOcupacion reporte = service.calcular(100, periodo, List.of(s1));

        assertEquals(0, reporte.getTotalCitasGlobal());
        assertEquals(3, reporte.getTotalCanceladasGlobal());
        assertEquals(0.0, reporte.getPorcentajeOcupacionGlobal());
        assertEquals(100.0, reporte.getPorcentajeCancelacionGlobal());
    }

    @Test
    @DisplayName("Lista de servicios vacía retorna reporte con ceros estables")
    void listaVacia() {
        ReporteOcupacion reporte = service.calcular(100, periodo, List.of());

        assertEquals(0, reporte.getTotalCitasGlobal());
        assertEquals(0, reporte.getTotalCanceladasGlobal());
        assertEquals(0.0, reporte.getHorasOcupadasGlobal());
        assertEquals(0.0, reporte.getHorasDisponiblesGlobal());
        assertEquals(0.0, reporte.getPorcentajeOcupacionGlobal());
        assertEquals(0.0, reporte.getPorcentajeCancelacionGlobal());
        assertTrue(reporte.getDetalleServicios().isEmpty());
    }
}
