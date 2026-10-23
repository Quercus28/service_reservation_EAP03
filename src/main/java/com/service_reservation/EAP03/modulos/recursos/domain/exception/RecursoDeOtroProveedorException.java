package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class RecursoDeOtroProveedorException extends RecursoDominioException {
    public RecursoDeOtroProveedorException(Integer idRecurso, Integer idServicio) {
        super("El recurso " + idRecurso + " no se puede usar en el servicio " + idServicio
                + " porque pertenece a otro proveedor.");
    }
}
