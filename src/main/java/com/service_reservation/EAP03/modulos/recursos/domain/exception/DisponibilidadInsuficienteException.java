package com.service_reservation.EAP03.modulos.recursos.domain.exception;

public class DisponibilidadInsuficienteException extends RecursoDominioException {

    private final int disponible;

    public DisponibilidadInsuficienteException(Integer idRecurso, int solicitado, int disponible) {
        super("No hay disponibilidad suficiente para el recurso " + idRecurso
                + ": se solicitaron " + solicitado + " unidades y solo hay " + disponible
                + " disponibles en ese rango de tiempo.");
        this.disponible = disponible;
    }

    public int getDisponible() { return disponible; }
}
