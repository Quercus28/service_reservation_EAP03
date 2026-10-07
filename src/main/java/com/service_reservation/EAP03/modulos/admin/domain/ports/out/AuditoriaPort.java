package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;

public interface AuditoriaPort {
    void registrar(RegistroAuditoria registro);
}
