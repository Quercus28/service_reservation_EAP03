package com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception;

public class ServicioNoPerteneceAlProveedorException extends RuntimeException {
    public ServicioNoPerteneceAlProveedorException(Integer servicioId) {
        super("El servicio " + servicioId + " no pertenece al proveedor autenticado.");
    }
}
