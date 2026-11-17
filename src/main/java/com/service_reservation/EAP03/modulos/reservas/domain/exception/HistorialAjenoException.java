package com.service_reservation.EAP03.modulos.reservas.domain.exception;

// El mensaje es fijo a propósito: no menciona al dueño ni al id pedido.
public class HistorialAjenoException extends ReservaDominioException {
    public HistorialAjenoException() {
        super("No tiene permiso para consultar el historial de otro cliente.");
    }
}