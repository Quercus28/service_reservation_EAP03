package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

public interface EliminarBloqueoRecursoUseCase {
    void ejecutar(Integer idRecurso, Integer idBloqueo);
}
