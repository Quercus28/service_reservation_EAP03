package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import java.time.LocalDateTime;

public interface ConsultarDisponibilidadUseCase {
    DisponibilidadRecurso ejecutar(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta);
}
