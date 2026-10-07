package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;

public interface Configurar2faUseCase {
    ResultadoConfiguracion2fa ejecutar(Long idUsuario);
}
