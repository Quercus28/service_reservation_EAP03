package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import org.springframework.stereotype.Service;

@Service
public class RegistrarAccesoDenegadoService implements RegistrarAccesoDenegadoUseCase {

    private final AuditoriaPort auditoriaPort;

    public RegistrarAccesoDenegadoService(AuditoriaPort auditoriaPort) {
        this.auditoriaPort = auditoriaPort;
    }

    @Override
    public void registrar(IntentoAccesoDenegado intento) {
        auditoriaPort.registrar(new RegistroAuditoria(
                intento.fecha(),
                intento.idUsuario(),
                null,
                AccionAdmin.ACCESO_DENEGADO,
                null,
                ResultadoAuditoria.DENEGADO,
                intento.metodo() + " " + intento.endpoint(),
                "email=" + intento.email() + ", ip=" + intento.ip() + ", motivo=sin ROLE_ADMIN"
        ));
    }
}
