package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarPrestamoRecursoServiceTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");
    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Mock private ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    @Mock private RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;
    @Mock private RecursoRepositoryPort recursoRepositoryPort;

    @InjectMocks private RegistrarPrestamoRecursoService service;

    private RegistrarPrestamoComando comando(int cantidad) {
        return new RegistrarPrestamoComando(1, 100, cantidad, DESDE, HASTA, PRECIO);
    }

    private Recurso recurso() {
        return Recurso.reconstruir(1, 7, "Camilla", PRECIO, 5,
                LocalDateTime.of(2026, 9, 1, 8, 0), null);
    }

    @Test
    @DisplayName("Registra el prestamo cuando hay disponibilidad suficiente")
    void registraCuandoAlcanza() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(recurso()));
        when(consultarDisponibilidadUseCase.ejecutar(1, DESDE, HASTA))
                .thenReturn(new DisponibilidadRecurso(1, DESDE, HASTA, 5, 1, 4));
        when(recursoPrestadoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RecursoPrestado resultado = service.ejecutar(comando(3));

        assertEquals(3, resultado.getCantidad());
        verify(recursoPrestadoRepositoryPort).guardar(any());
    }

    @Test
    @DisplayName("Rechaza cuando la cantidad pedida supera lo disponible")
    void rechazaCuandoNoAlcanza() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(recurso()));
        when(consultarDisponibilidadUseCase.ejecutar(1, DESDE, HASTA))
                .thenReturn(new DisponibilidadRecurso(1, DESDE, HASTA, 5, 4, 1));

        assertThrows(DisponibilidadInsuficienteException.class, () -> service.ejecutar(comando(3)));
        verify(recursoPrestadoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Falla si el recurso no existe al tomar el bloqueo")
    void fallaSiNoExisteElRecurso() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(comando(1)));
        verify(recursoPrestadoRepositoryPort, never()).guardar(any());
    }
}
