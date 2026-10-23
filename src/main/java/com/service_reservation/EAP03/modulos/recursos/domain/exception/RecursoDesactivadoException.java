package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoDesactivadoException extends RecursoDominioException {
    public RecursoDesactivadoException(Integer idRecurso) {
        super("El recurso " + idRecurso + " está desactivado y no se puede asociar a un servicio.");
    }

    private RecursoDesactivadoException(String mensaje) {
        super(mensaje);
    }

    // Para el registro manual de un préstamo.
    public static RecursoDesactivadoException alPrestar(String nombre) {
        return new RecursoDesactivadoException("No se puede prestar: el recurso '" + nombre
                + "' ya no está disponible porque el proveedor lo desactivó.");
    }

    // Para cuando el recurso lo necesita una reserva.
    public static RecursoDesactivadoException alReservar(String nombre) {
        return new RecursoDesactivadoException("No se puede reservar: el recurso '" + nombre
                + "' ya no está disponible porque el proveedor lo desactivó.");
    }
}
