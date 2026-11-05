package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;

public interface Activar2faUseCase {
    TokenRespuesta ejecutar(Long idUsuario, String codigoTotp);
}
