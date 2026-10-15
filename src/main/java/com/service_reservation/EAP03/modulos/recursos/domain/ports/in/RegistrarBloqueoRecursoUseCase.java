package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;

public interface RegistrarBloqueoRecursoUseCase {
    BloqueoRecurso ejecutar(RegistrarBloqueoComando comando);
}
