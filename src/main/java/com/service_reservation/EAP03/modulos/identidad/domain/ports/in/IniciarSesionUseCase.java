package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;

public interface IniciarSesionUseCase {
    TokenRespuesta ejecutar(IniciarSesionComando comando);
}
