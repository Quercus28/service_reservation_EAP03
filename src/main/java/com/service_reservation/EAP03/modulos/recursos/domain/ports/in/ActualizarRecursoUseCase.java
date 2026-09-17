package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.ActualizarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;

public interface ActualizarRecursoUseCase {
    Recurso ejecutar(ActualizarRecursoComando comando);
}
