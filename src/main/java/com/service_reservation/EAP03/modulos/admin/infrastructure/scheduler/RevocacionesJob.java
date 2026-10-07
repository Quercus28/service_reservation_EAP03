package com.service_reservation.EAP03.modulos.admin.infrastructure.scheduler;

import com.service_reservation.EAP03.modulos.admin.domain.ports.in.ProcesarRevocacionesPendientesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RevocacionesJob {

    private static final Logger log = LoggerFactory.getLogger(RevocacionesJob.class);

    private final ProcesarRevocacionesPendientesUseCase procesarRevocacionesPendientesUseCase;

    public RevocacionesJob(ProcesarRevocacionesPendientesUseCase procesarRevocacionesPendientesUseCase) {
        this.procesarRevocacionesPendientesUseCase = procesarRevocacionesPendientesUseCase;
    }

    // Run every day at midnight. Can be configured via properties in a real environment.
    @Scheduled(cron = "0 0 0 * * ?")
    public void ejecutarProcesamiento() {
        log.info("Iniciando procesamiento de revocaciones pendientes...");
        try {
            int completadas = procesarRevocacionesPendientesUseCase.procesar();
            log.info("Procesamiento finalizado. Revocaciones completadas: {}", completadas);
        } catch (Exception e) {
            log.error("Error al procesar revocaciones pendientes", e);
        }
    }
}
