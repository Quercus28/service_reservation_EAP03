package com.service_reservation.EAP03.modulos.reservas.domain.exception;

// Quien intenta cambiar el estado no es un proveedor con permiso activo (por ejemplo, un cliente).
public class UsuarioNoEsProveedorException extends ReservaDominioException {
    public UsuarioNoEsProveedorException() {
        super("Solo el proveedor dueño de la reserva, con su permiso activo, puede cambiar su estado.");
    }
}
