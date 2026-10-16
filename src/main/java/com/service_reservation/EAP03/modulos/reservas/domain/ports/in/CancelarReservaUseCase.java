package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

public interface CancelarReservaUseCase {
    Reserva ejecutar(CancelarReservaComando comando);
}
