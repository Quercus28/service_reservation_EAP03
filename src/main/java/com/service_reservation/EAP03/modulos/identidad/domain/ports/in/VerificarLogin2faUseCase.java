package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;

public interface VerificarLogin2faUseCase {
    TokenRespuesta ejecutar(String tokenTemporal, String codigoTotp);
}
