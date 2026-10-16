package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;

public interface CrearReservaUseCase {
    Reserva ejecutar(CrearReservaComando comando);
}
