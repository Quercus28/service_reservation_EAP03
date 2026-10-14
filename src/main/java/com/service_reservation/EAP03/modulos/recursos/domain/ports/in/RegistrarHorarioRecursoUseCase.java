package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarHorarioComando;

public interface RegistrarHorarioRecursoUseCase {
    HorarioRecurso ejecutar(RegistrarHorarioComando comando);
}
