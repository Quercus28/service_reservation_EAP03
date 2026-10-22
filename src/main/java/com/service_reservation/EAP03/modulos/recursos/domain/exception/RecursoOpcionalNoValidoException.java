package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import java.util.List;

// El cliente pidió como extra un recurso que el servicio no ofrece como opcional.
// servicio y recursos traen el nombre entre comillas, o el número si no se encontró.
public class RecursoOpcionalNoValidoException extends RecursoDominioException {
    public RecursoOpcionalNoValidoException(String servicio, List<String> recursos) {
        super(recursos.size() == 1
                ? "No se puede pedir el recurso " + recursos.get(0) + " con el servicio " + servicio
                        + ": no está entre sus recursos opcionales."
                : "No se pueden pedir los recursos " + String.join(", ", recursos) + " con el servicio "
                        + servicio + ": no están entre sus recursos opcionales.");
    }
}
