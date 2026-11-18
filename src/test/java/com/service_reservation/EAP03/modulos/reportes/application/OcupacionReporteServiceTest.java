package com.service_reservation.EAP03.modulos.reportes.application;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.DatosOcupacionServicio;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.out.OcupacionDataPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcupacionReporteServiceTest {

    @Mock
    private OcupacionDataPort ocupacionDataPort;

    @InjectMocks
    private OcupacionReporteService service;

    @Test
    @DisplayName("Ejecuta exitosamente el reporte coordinando el puerto de datos y el dominio")
    void ejecutarReporteExitoso() {
        Integer proveedorId = 5;
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 15);
        ConsultaOcupacionComando comando = new ConsultaOcupacionComando(proveedorId, inicio, fin);

        DatosOcupacionServicio dato = DatosOcupacionServicio.builder(10, "Servicio Test")
                .duracionMinutosServicio(60)
                .totalCitas(2)
                .totalCanceladas(1)
                .minutosOcupados(120)
                .minutosDisponibles(600)
                .build();

        when(ocupacionDataPort.obtenerDatosOcupacion(eq(proveedorId), any(PeriodoReporte.class)))
                .thenReturn(List.of(dato));

        ReporteOcupacion reporte = service.ejecutar(comando);

        assertNotNull(reporte);
        assertEquals(proveedorId, reporte.getIdProveedor());
        assertEquals(2, reporte.getTotalCitasGlobal());
        assertEquals(1, reporte.getTotalCanceladasGlobal());
        assertEquals(2.0, reporte.getHorasOcupadasGlobal());
        assertEquals(10.0, reporte.getHorasDisponiblesGlobal());
        assertEquals(20.0, reporte.getPorcentajeOcupacionGlobal());
        assertEquals(33.33, reporte.getPorcentajeCancelacionGlobal());

        verify(ocupacionDataPort, times(1)).obtenerDatosOcupacion(eq(proveedorId), any(PeriodoReporte.class));
    }

    @Test
    @DisplayName("Rechaza inmediatamente comando con endDate anterior a startDate")
    void rechazarFechasInvertidas() {
        ConsultaOcupacionComando comando = new ConsultaOcupacionComando(
                5, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 10));

        assertThrows(PeriodoReporteInvalidoException.class, () -> service.ejecutar(comando));
        verifyNoInteractions(ocupacionDataPort);
    }
}
