package com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;

public interface ActualizarServicioUseCase {

    ServicioResponse ejecutar(Integer id, ActualizarServicioRequest request);
}
