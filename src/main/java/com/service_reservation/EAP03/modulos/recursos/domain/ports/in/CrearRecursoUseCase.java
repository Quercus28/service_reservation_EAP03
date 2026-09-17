package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.CrearRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;

public interface CrearRecursoUseCase {
    Recurso ejecutar(CrearRecursoComando comando);
}
