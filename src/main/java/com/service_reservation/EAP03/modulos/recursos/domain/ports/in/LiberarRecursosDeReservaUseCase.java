package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

// Para el módulo de reservas: devuelve los recursos de una reserva cancelada. Los préstamos no
// se borran, solo se marcan como liberados. Si la reserva no tenía recursos, no hace nada.
public interface LiberarRecursosDeReservaUseCase {
    void ejecutar(Integer idReserva);
}
