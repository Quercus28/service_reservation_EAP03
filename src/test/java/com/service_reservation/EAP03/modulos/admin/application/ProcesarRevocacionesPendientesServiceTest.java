package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.RevocacionPermisoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarRevocacionesPendientesServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 0, 0);

    @Mock private RevocacionPermisoRepositoryPort revocacionRepository;
    @Mock private CierreRevocacionPendiente cierreRevocacion;

    private ProcesarRevocacionesPendientesService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-17T00:00:00Z"), ZoneOffset.UTC);
        service = new ProcesarRevocacionesPendientesService(revocacionRepository, cierreRevocacion, reloj);
    }

    private static RevocacionPermiso pending(long idUsuario) {
        return RevocacionPermiso.conPlazoDeGracia(idUsuario, "Fraude", 1L, AHORA.minusDays(6));
    }

    @Test
    @DisplayName("Cuenta solo las revocaciones que se completaron y sigue aunque una falle")
    void countsCompletedAndContinuesOnError() {
        // Arrange
        RevocacionPermiso completada = pending(1L);
        RevocacionPermiso enPlazo = pending(2L);
        RevocacionPermiso conError = pending(3L);
        RevocacionPermiso otraCompletada = pending(4L);
        when(revocacionRepository.listar(EstadoRevocacion.PENDIENTE_CANCELACION))
                .thenReturn(List.of(completada, enPlazo, conError, otraCompletada));
        when(cierreRevocacion.procesar(completada, AHORA)).thenReturn(true);
        when(cierreRevocacion.procesar(enPlazo, AHORA)).thenReturn(false);
        when(cierreRevocacion.procesar(conError, AHORA)).thenThrow(new IllegalStateException("fallo"));
        when(cierreRevocacion.procesar(otraCompletada, AHORA)).thenReturn(true);

        // Act
        int total = service.procesar();

        // Assert
        assertEquals(2, total);
        verify(cierreRevocacion).procesar(otraCompletada, AHORA);
    }

    @Test
    @DisplayName("Sin revocaciones pendientes no completa ninguna")
    void noPendingRevocations() {
        when(revocacionRepository.listar(EstadoRevocacion.PENDIENTE_CANCELACION)).thenReturn(List.of());

        assertEquals(0, service.procesar());
        verifyNoInteractions(cierreRevocacion);
    }
}
