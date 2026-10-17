package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class CancelacionFueraDeTiempoException extends ReservaDominioException {

    private CancelacionFueraDeTiempoException(String mensaje) {
        super(mensaje);
    }

    // La reserva todavía no empezó, pero falta menos tiempo del mínimo para cancelarla.
    public static CancelacionFueraDeTiempoException faltaPocoTiempo(Integer idReserva, int anticipacionMinimaHoras) {
        return new CancelacionFueraDeTiempoException("La reserva " + idReserva
                + " ya no se puede cancelar: hay que cancelar al menos "
                + anticipacionMinimaHoras + " horas antes de la hora de inicio.");
    }

    // La hora de inicio ya llegó o ya pasó.
    public static CancelacionFueraDeTiempoException yaEmpezo(Integer idReserva) {
        return new CancelacionFueraDeTiempoException("La reserva " + idReserva
                + " no se puede cancelar porque ya empezó o ya pasó.");
    }
}
