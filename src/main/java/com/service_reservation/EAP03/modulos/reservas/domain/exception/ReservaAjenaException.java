package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class ReservaAjenaException extends ReservaDominioException {
    public ReservaAjenaException(Integer idReserva) {
        super("No tiene permiso sobre la reserva " + idReserva + ": pertenece a otro cliente.");
    }

    private ReservaAjenaException(String mensaje) {
        super(mensaje);
    }

    // El proveedor que intenta cambiar el estado no es el dueño de la reserva.
    public static ReservaAjenaException deOtroProveedor(Integer idReserva) {
        return new ReservaAjenaException("No tiene permiso sobre la reserva " + idReserva
                + ": pertenece a otro proveedor.");
    }
}
