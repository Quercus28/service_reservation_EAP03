package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDeOtroProveedorException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoYaAsociadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.ServicioNoExisteException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.AsociarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsociarRecursoAServicioServiceTest {

    private static final Integer ID_SERVICIO = 10;
    private static final Integer ID_RECURSO = 1;
    private static final Integer ID_PROVEEDOR = 7;

    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;

    @InjectMocks private AsociarRecursoAServicioService service;

    private AsociarRecursoComando comando(int cantidad) {
        return new AsociarRecursoComando(ID_SERVICIO, ID_RECURSO, cantidad);
    }

    private ServicioResponse servicioDe(Integer idProveedor) {
        return new ServicioResponse(ID_SERVICIO, idProveedor, "Masaje", 60, null);
    }

    private Recurso recurso(LocalDateTime eliminadoEn) {
        return Recurso.reconstruir(ID_RECURSO, ID_PROVEEDOR, "Camilla", new BigDecimal("1000.00"), 3,
                LocalDateTime.of(2026, 9, 1, 8, 0), eliminadoEn);
    }

    @Test
    @DisplayName("Rechaza cuando el recurso no existe")
    void rejectsUnknownResource() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(ID_PROVEEDOR));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(comando(1)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Asocia el recurso cuando servicio y recurso son del mismo proveedor")
    void asociaCuandoTodoEstaBien() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(ID_PROVEEDOR));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO)).thenReturn(Optional.of(recurso(null)));
        when(servicioRecursoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        ServicioRecurso resultado = service.ejecutar(comando(2));

        assertEquals(2, resultado.getCantidad());
    }

    @Test
    @DisplayName("Rechaza cuando el servicio no existe")
    void rechazaServicioInexistente() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenThrow(new ServicioNoEncontradoException(ID_SERVICIO));

        assertThrows(ServicioNoExisteException.class, () -> service.ejecutar(comando(1)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza cuando el recurso es de otro proveedor")
    void rechazaRecursoDeOtroProveedor() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(99));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO)).thenReturn(Optional.of(recurso(null)));

        assertThrows(RecursoDeOtroProveedorException.class, () -> service.ejecutar(comando(1)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza cuando el recurso está desactivado")
    void rechazaRecursoDesactivado() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(ID_PROVEEDOR));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO))
                .thenReturn(Optional.of(recurso(LocalDateTime.of(2026, 10, 1, 9, 0))));

        assertThrows(RecursoDesactivadoException.class, () -> service.ejecutar(comando(1)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza cuando el recurso ya estaba asociado al servicio")
    void rechazaDuplicado() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(ID_PROVEEDOR));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO)).thenReturn(Optional.of(recurso(null)));
        when(servicioRecursoRepositoryPort.existePorServicioYRecurso(ID_SERVICIO, ID_RECURSO)).thenReturn(true);

        assertThrows(RecursoYaAsociadoException.class, () -> service.ejecutar(comando(1)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza una cantidad de cero")
    void rechazaCantidadCero() {
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(servicioDe(ID_PROVEEDOR));
        when(recursoRepositoryPort.buscarPorId(ID_RECURSO)).thenReturn(Optional.of(recurso(null)));

        assertThrows(DatosInvalidosException.class, () -> service.ejecutar(comando(0)));
        verify(servicioRecursoRepositoryPort, never()).guardar(any());
    }
}
