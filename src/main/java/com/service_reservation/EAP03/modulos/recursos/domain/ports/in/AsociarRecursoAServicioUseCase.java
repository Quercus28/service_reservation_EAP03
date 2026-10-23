package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.AsociarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;

public interface AsociarRecursoAServicioUseCase {
    ServicioRecurso ejecutar(AsociarRecursoComando comando);
}
