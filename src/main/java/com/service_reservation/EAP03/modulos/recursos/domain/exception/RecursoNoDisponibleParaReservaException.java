package com.service_reservation.EAP03.modulos.recursos.domain.exception;

// Un recurso que la reserva necesita no se puede apartar. El mensaje usa el nombre del
// recurso para que el cliente entienda cuál falta.
public class RecursoNoDisponibleParaReservaException extends RecursoDominioException {

    private RecursoNoDisponibleParaReservaException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoDisponibleParaReservaException desactivado(String nombre) {
        return new RecursoNoDisponibleParaReservaException("No se puede reservar: el recurso '" + nombre
                + "' ya no está disponible porque el proveedor lo desactivó.");
    }

    public static RecursoNoDisponibleParaReservaException sinUnidades(String nombre, int pedidas, int libres) {
        return new RecursoNoDisponibleParaReservaException("No se puede reservar: del recurso '" + nombre
                + "' se necesitan " + pedidas + " unidades y en ese horario solo quedan " + libres + ".");
    }

    public static RecursoNoDisponibleParaReservaException noOperativo(String nombre) {
        return new RecursoNoDisponibleParaReservaException("No se puede reservar: el recurso '" + nombre
                + "' no se puede usar en ese horario (está fuera de su horario o bloqueado).");
    }
}
