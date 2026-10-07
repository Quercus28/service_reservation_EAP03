package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;

public interface RegistrarAccesoDenegadoUseCase {
    void registrar(IntentoAccesoDenegado intento);
}
