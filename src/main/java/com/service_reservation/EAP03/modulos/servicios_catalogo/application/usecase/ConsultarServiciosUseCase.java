package com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;

public interface ConsultarServiciosUseCase {

    PaginaResponse<ServicioResponse> ejecutar(Integer idProveedor, String nombre, int pagina, int tamano);
}
