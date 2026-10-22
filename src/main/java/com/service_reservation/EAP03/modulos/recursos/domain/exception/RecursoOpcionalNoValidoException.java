package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import java.util.List;

// El cliente pidió como extra un recurso que el servicio no ofrece como opcional.
// recursos trae el nombre de cada recurso entre comillas, o su número si no existe.
public class RecursoOpcionalNoValidoException extends RecursoDominioException {
    public RecursoOpcionalNoValidoException(Integer idServicio, List<String> recursos) {
        super(recursos.size() == 1
                ? "No se puede pedir el recurso " + recursos.get(0) + " con el servicio " + idServicio
                        + ": no está entre sus recursos opcionales."
                : "No se pueden pedir los recursos " + String.join(", ", recursos) + " con el servicio "
                        + idServicio + ": no están entre sus recursos opcionales.");
    }
}
