package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import java.util.Collection;
import java.util.stream.Collectors;

// El cliente pidió como extra un recurso que el servicio no ofrece como opcional.
public class RecursoOpcionalNoValidoException extends RecursoDominioException {
    public RecursoOpcionalNoValidoException(Integer idServicio, Collection<Integer> idsRecurso) {
        super("No se pueden pedir los recursos " + idsRecurso.stream().map(String::valueOf).collect(Collectors.joining(", "))
                + " con el servicio " + idServicio + ": no están entre sus recursos opcionales.");
    }
}
