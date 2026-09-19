package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;

public interface ConsultarRecursosUseCase {
    ResultadoPaginado<Recurso> ejecutar(Long idProveedor, int pagina, int tamano);
}
