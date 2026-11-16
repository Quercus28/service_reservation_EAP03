package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;

// Para otros módulos: guarda en la auditoría de admin un intento de acceso o de cambio no
// permitido, sin que ese módulo tenga que escribir directo en la tabla de admin.
public interface RegistrarAuditoriaUseCase {
    void ejecutar(EventoAuditoria evento);
}
