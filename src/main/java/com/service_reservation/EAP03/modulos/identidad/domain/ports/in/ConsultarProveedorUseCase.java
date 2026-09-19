package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;

import java.util.Optional;

public interface ConsultarProveedorUseCase {
    Optional<Long> buscarIdProveedorPorUsuario(Long idUsuario);
}
