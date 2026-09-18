package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadServiceTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");
    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    @InjectMocks private ConsultarDisponibilidadService service;

    @Test
    @DisplayName("Resta del stock solo los prestamos que realmente se solapan")
    void calculaDisponibilidad() {
        Recurso recurso = Recurso.reconstruir(1, 7, "Camilla", PRECIO, 5,
                LocalDateTime.of(2026, 9, 1, 8, 0), null);

        RecursoPrestado solapaA = RecursoPrestado.nuevo(1, 100, 2,
                LocalDateTime.of(2026, 10, 1, 9, 0), LocalDateTime.of(2026, 10, 1, 11, 0), PRECIO);
        RecursoPrestado solapaB = RecursoPrestado.nuevo(1, 101, 1,
                LocalDateTime.of(2026, 10, 1, 11, 30), LocalDateTime.of(2026, 10, 1, 13, 0), PRECIO);
        RecursoPrestado enElBorde = RecursoPrestado.nuevo(1, 102, 3,
                HASTA, LocalDateTime.of(2026, 10, 1, 14, 0), PRECIO);

        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recurso));
        when(recursoPrestadoRepositoryPort.buscarQueSeSolapan(eq(1), any(), any()))
                .thenReturn(List.of(solapaA, solapaB, enElBorde));

        DisponibilidadRecurso resultado = service.ejecutar(1, DESDE, HASTA);

        assertEquals(5, resultado.stockTotal());
        assertEquals(3, resultado.comprometido());
        assertEquals(2, resultado.disponible());
    }

    @Test
    @DisplayName("Sin prestamos, todo el stock esta disponible")
    void sinPrestamos() {
        Recurso recurso = Recurso.reconstruir(1, 7, "Camilla", PRECIO, 4,
                LocalDateTime.of(2026, 9, 1, 8, 0), null);

        when(recursoRepositoryPort.buscarPorId(1)).thenReturn(Optional.of(recurso));
        when(recursoPrestadoRepositoryPort.buscarQueSeSolapan(eq(1), any(), any()))
                .thenReturn(List.of());

        DisponibilidadRecurso resultado = service.ejecutar(1, DESDE, HASTA);

        assertEquals(0, resultado.comprometido());
        assertEquals(4, resultado.disponible());
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void recursoInexistente() {
        when(recursoRepositoryPort.buscarPorId(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.ejecutar(99, DESDE, HASTA));
    }

    @Test
    @DisplayName("Falla si el rango de fechas es invalido")
    void rangoInvalido() {
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(1, HASTA, DESDE));
        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(1, null, HASTA));
    }
}
