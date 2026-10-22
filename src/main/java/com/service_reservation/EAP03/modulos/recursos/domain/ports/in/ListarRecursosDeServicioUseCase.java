package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;

import java.util.List;

public interface ListarRecursosDeServicioUseCase {
    List<ServicioRecurso> ejecutar(Integer idServicio);
}
