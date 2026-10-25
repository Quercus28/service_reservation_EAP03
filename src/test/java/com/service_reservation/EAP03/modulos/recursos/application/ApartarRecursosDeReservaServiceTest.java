package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoDisponibleParaReservaException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoOperativoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoOpcionalNoValidoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartarRecursosDeReservaServiceTest {

    private static final Integer ID_RESERVA = 50;
    private static final Integer ID_SERVICIO = 10;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);

    @Mock private ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;
    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;
    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;

    @InjectMocks private ApartarRecursosDeReservaService service;

    private static Recurso resource(int id, String nombre) {
        return Recurso.reconstruir(id, 7, nombre, new BigDecimal("2000.00"), 5, INICIO.minusMonths(1), null);
    }

    private void serviceOffers(ServicioRecurso... asociaciones) {
        when(servicioRecursoRepositoryPort.buscarPorServicio(ID_SERVICIO)).thenReturn(List.of(asociaciones));
    }

    private void lockedResource(Recurso recurso) {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(recurso.getId())).thenReturn(Optional.of(recurso));
    }

    @Test
    @DisplayName("Aparta solo los recursos elegidos, en orden de id y cobrando precio por cantidad")
    void reservesChosenResourcesInIdOrder() {
        // Arrange
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 2),
                ServicioRecurso.reconstruir(2, ID_SERVICIO, 1, 1),
                ServicioRecurso.reconstruir(3, ID_SERVICIO, 2, 1));
        lockedResource(resource(1, "Toalla"));
        lockedResource(resource(3, "Aceite"));
        when(registrarPrestamoRecursoUseCase.ejecutar(any())).thenAnswer(inv -> {
            RegistrarPrestamoComando c = inv.getArgument(0);
            return RecursoPrestado.nuevo(c.idRecurso(), c.idServicioPrestado(), c.cantidad(),
                    c.fechaInicio(), c.fechaFin(), c.precioTotal());
        });

        // Act
        List<RecursoPrestado> prestamos = service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(3, 1));

        // Assert
        assertEquals(List.of(1, 3), prestamos.stream().map(RecursoPrestado::getIdRecurso).toList());
        ArgumentCaptor<RegistrarPrestamoComando> captor = ArgumentCaptor.forClass(RegistrarPrestamoComando.class);
        verify(registrarPrestamoRecursoUseCase, times(2)).ejecutar(captor.capture());
        RegistrarPrestamoComando aceite = captor.getAllValues().get(1);
        assertEquals(ID_RESERVA, aceite.idServicioPrestado());
        assertEquals(2, aceite.cantidad());
        assertEquals(new BigDecimal("4000.00"), aceite.precioTotal());
        assertEquals(INICIO, aceite.fechaInicio());
        assertEquals(FIN, aceite.fechaFin());
    }

    @Test
    @DisplayName("Sin recursos elegidos no aparta nada")
    void nothingChosenReservesNothing() {
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 1));

        assertTrue(service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, null).isEmpty());
        assertTrue(service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of()).isEmpty());
        verifyNoInteractions(registrarPrestamoRecursoUseCase);
    }

    @Test
    @DisplayName("Rechaza un recurso que el servicio no ofrece, usando los nombres de servicio y recurso")
    void rejectsResourceNotOfferedWithNames() {
        // Arrange
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 1));
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenReturn(new ServicioResponse(ID_SERVICIO, 7, "Masaje", 60, null));
        when(recursoRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(resource(8, "Toalla")));

        // Act
        RecursoOpcionalNoValidoException ex = assertThrows(RecursoOpcionalNoValidoException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(8)));

        // Assert
        assertEquals("No se puede pedir el recurso 'Toalla' con el servicio 'Masaje': no está entre sus recursos opcionales.",
                ex.getMessage());
        verifyNoInteractions(registrarPrestamoRecursoUseCase);
    }

    @Test
    @DisplayName("Usa los números cuando el servicio o el recurso pedido no existen")
    void rejectsResourceNotOfferedWithIds() {
        // Arrange
        serviceOffers();
        when(obtenerServicioPorIdUseCase.ejecutar(ID_SERVICIO)).thenThrow(new ServicioNoEncontradoException(ID_SERVICIO));
        when(recursoRepositoryPort.buscarPorId(8)).thenReturn(Optional.empty());
        when(recursoRepositoryPort.buscarPorId(9)).thenReturn(Optional.empty());

        // Act
        RecursoOpcionalNoValidoException ex = assertThrows(RecursoOpcionalNoValidoException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(9, 8)));

        // Assert
        assertEquals("No se pueden pedir los recursos 8, 9 con el servicio 10: no están entre sus recursos opcionales.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Falla si el recurso asociado ya no existe al tomar el bloqueo")
    void failsWhenResourceDisappeared() {
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 1));
        when(recursoRepositoryPort.buscarPorIdConBloqueo(3)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(3)));
    }

    @Test
    @DisplayName("Un recurso desactivado se informa como no reservable con su nombre")
    void deactivatedResourceMessageTalksAboutReservation() {
        // Arrange
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 1));
        lockedResource(resource(3, "Aceite"));
        when(registrarPrestamoRecursoUseCase.ejecutar(any())).thenThrow(RecursoDesactivadoException.alPrestar("Aceite"));

        // Act
        RecursoDesactivadoException ex = assertThrows(RecursoDesactivadoException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(3)));

        // Assert
        assertEquals(RecursoDesactivadoException.alReservar("Aceite").getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("La falta de unidades se informa con lo que se necesita y lo que queda")
    void insufficientUnitsMessageTalksAboutReservation() {
        // Arrange
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 2));
        lockedResource(resource(3, "Aceite"));
        when(registrarPrestamoRecursoUseCase.ejecutar(any())).thenThrow(new DisponibilidadInsuficienteException(3, 2, 1));

        // Act
        RecursoNoDisponibleParaReservaException ex = assertThrows(RecursoNoDisponibleParaReservaException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(3)));

        // Assert
        assertEquals(RecursoNoDisponibleParaReservaException.sinUnidades("Aceite", 2, 1).getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("Un recurso fuera de horario o bloqueado se informa como no operativo")
    void notOperationalMessageTalksAboutReservation() {
        // Arrange
        serviceOffers(ServicioRecurso.reconstruir(1, ID_SERVICIO, 3, 1));
        lockedResource(resource(3, "Aceite"));
        when(registrarPrestamoRecursoUseCase.ejecutar(any())).thenThrow(new RecursoNoOperativoException(3, INICIO, FIN));

        // Act
        RecursoNoDisponibleParaReservaException ex = assertThrows(RecursoNoDisponibleParaReservaException.class,
                () -> service.ejecutar(ID_RESERVA, ID_SERVICIO, INICIO, FIN, List.of(3)));

        // Assert
        assertEquals(RecursoNoDisponibleParaReservaException.noOperativo("Aceite").getMessage(), ex.getMessage());
    }
}
