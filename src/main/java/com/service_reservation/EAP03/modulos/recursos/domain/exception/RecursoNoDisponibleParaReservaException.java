package com.service_reservation.EAP03.modulos.recursos.domain.exception;

// Un recurso que la reserva necesita no se puede apartar. El mensaje usa el nombre del
// recurso para que el cliente entienda cuál falta. Si el recurso está desactivado, el error
// es RecursoDesactivadoException, que sale del registro del préstamo.
public class RecursoNoDisponibleParaReservaException extends RecursoDominioException {

    private RecursoNoDisponibleParaReservaException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoDisponibleParaReservaException sinUnidades(String nombre, int pedidas, int libres) {
        return new RecursoNoDisponibleParaReservaException("No se puede reservar: del recurso '" + nombre
                + "' " + (pedidas == 1 ? "se necesita " : "se necesitan ") + unidades(pedidas)
                + " y en ese horario solo " + (libres == 1 ? "queda " : "quedan ") + libres + ".");
    }

    public static RecursoNoDisponibleParaReservaException noOperativo(String nombre) {
        return new RecursoNoDisponibleParaReservaException("No se puede reservar: el recurso '" + nombre
                + "' no se puede usar en ese horario (está fuera de su horario o bloqueado).");
    }
}
