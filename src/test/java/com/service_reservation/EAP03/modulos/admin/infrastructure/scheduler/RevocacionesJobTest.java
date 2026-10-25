package com.service_reservation.EAP03.modulos.admin.infrastructure.scheduler;

import com.service_reservation.EAP03.modulos.admin.domain.ports.in.ProcesarRevocacionesPendientesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevocacionesJobTest {

    @Mock private ProcesarRevocacionesPendientesUseCase procesarRevocacionesPendientesUseCase;

    @InjectMocks private RevocacionesJob job;

    @Test
    @DisplayName("Ejecuta el procesamiento de revocaciones pendientes")
    void runsProcessing() {
        when(procesarRevocacionesPendientesUseCase.procesar()).thenReturn(2);

        job.ejecutarProcesamiento();

        verify(procesarRevocacionesPendientesUseCase).procesar();
    }

    @Test
    @DisplayName("Un error en el procesamiento no detiene el job")
    void errorDoesNotStopJob() {
        when(procesarRevocacionesPendientesUseCase.procesar()).thenThrow(new IllegalStateException("fallo"));

        assertDoesNotThrow(() -> job.ejecutarProcesamiento());
    }
}
