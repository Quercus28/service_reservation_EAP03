package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

public interface Activar2faUseCase {
    void ejecutar(Long idUsuario, String codigoTotp);
}
