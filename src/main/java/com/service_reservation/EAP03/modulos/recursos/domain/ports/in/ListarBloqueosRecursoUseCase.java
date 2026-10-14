package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;

public interface ListarBloqueosRecursoUseCase {
    ResultadoPaginado<BloqueoRecurso> ejecutar(Integer idRecurso, int pagina, int tamano);
}
