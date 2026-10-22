package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoDesactivadoException extends RecursoDominioException {
    public RecursoDesactivadoException(Integer idRecurso) {
        super("El recurso " + idRecurso + " está desactivado y no se puede asociar a un servicio.");
    }

    private RecursoDesactivadoException(String mensaje) {
        super(mensaje);
    }

    // Para un préstamo, sea manual o al reservar.
    public static RecursoDesactivadoException alPrestar(String nombre) {
        return new RecursoDesactivadoException("No se puede reservar: el recurso '" + nombre
                + "' ya no está disponible porque el proveedor lo desactivó.");
    }
}
