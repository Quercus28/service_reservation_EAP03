package com.service_reservation.EAP03.modulos.admin.infrastructure.events;

import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.events.UsuarioRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RegistroProveedorListener {

    private static final Logger log = LoggerFactory.getLogger(RegistroProveedorListener.class);
    private final GestionarSolicitudesProveedorUseCase gestionarSolicitudesProveedorUseCase;

    public RegistroProveedorListener(GestionarSolicitudesProveedorUseCase gestionarSolicitudesProveedorUseCase) {
        this.gestionarSolicitudesProveedorUseCase = gestionarSolicitudesProveedorUseCase;
    }

    @EventListener
    public void onUsuarioRegistrado(UsuarioRegistradoEvent event) {
        if ("ROLE_PROVEEDOR".equals(event.rolNormalizado()) || "PROVEEDOR".equals(event.rolNormalizado())) {
            log.info("Recibido evento de registro para proveedor con idUsuario: {}. Generando solicitud de aprobación...", event.idUsuario());
            gestionarSolicitudesProveedorUseCase.registrarSolicitud(event.idUsuario());
            log.info("Solicitud de aprobación generada correctamente para el proveedor {}", event.idUsuario());
        }
    }
}
