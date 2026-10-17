package com.service_reservation.EAP03.modulos.reservas.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Lee la configuración del módulo de reservas desde application.yaml.
@Component
public class ReservaProperties {

    // Cuántas horas antes de la hora de inicio se puede cancelar como máximo.
    // Por ejemplo, con 2, una reserva de las 15:00 se puede cancelar hasta las 13:00.
    private final int anticipacionMinimaHorasCancelacion;

    public ReservaProperties(@Value("${reservas.cancelacion.anticipacion-minima-horas}") int anticipacionMinimaHorasCancelacion) {
        this.anticipacionMinimaHorasCancelacion = anticipacionMinimaHorasCancelacion;
    }

    public int getAnticipacionMinimaHorasCancelacion() {
        return anticipacionMinimaHorasCancelacion;
    }
}
