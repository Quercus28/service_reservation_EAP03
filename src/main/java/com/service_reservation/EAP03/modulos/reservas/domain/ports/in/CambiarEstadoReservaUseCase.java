package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.CambiarEstadoReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

// HU20: el proveedor dueño confirma o finaliza una de sus reservas.
public interface CambiarEstadoReservaUseCase {
    Reserva ejecutar(CambiarEstadoReservaComando comando);
}
