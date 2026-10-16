package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class CancelacionFueraDeTiempoException extends ReservaDominioException {
    public CancelacionFueraDeTiempoException(Integer idReserva, int anticipacionMinimaHoras) {
        super("La reserva " + idReserva + " ya no se puede cancelar: hay que cancelar al menos "
                + anticipacionMinimaHoras + " horas antes de la hora de inicio.");
    }
}
