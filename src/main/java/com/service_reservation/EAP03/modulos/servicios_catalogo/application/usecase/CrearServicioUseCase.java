package com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;

public interface CrearServicioUseCase {

    ServicioResponse ejecutar(CrearServicioRequest request);
}
