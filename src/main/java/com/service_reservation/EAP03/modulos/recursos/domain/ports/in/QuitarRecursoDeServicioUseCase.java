package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

public interface QuitarRecursoDeServicioUseCase {
    void ejecutar(Integer idServicio, Integer idRecurso);
}
