package com.service_reservation.EAP03.modulos.reservas.domain.exception;

public class UsuarioNoEsClienteException extends ReservaDominioException {
    public UsuarioNoEsClienteException() {
        super("El usuario autenticado no tiene un perfil de cliente asociado.");
    }
}
