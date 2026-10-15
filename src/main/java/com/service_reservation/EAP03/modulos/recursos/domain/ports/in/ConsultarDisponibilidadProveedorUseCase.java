package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;

public interface ConsultarDisponibilidadProveedorUseCase {
    ResultadoPaginado<RecursoConHorarios> ejecutar(Integer idProveedor, int pagina, int tamano);
}
