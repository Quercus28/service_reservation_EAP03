package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/solicitudes")
public class SolicitudProveedorController {

    private final GestionarSolicitudesProveedorUseCase useCase;

    public SolicitudProveedorController(GestionarSolicitudesProveedorUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/proveedor")
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<SolicitudAprobacionProveedor> solicitarAprobacion(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.registrarSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @PostMapping("/proveedor/reenviar")
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<SolicitudAprobacionProveedor> reenviarSolicitud(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.reenviarSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/proveedor/me")
    @PreAuthorize("hasRole('PROVEEDOR')")
    public ResponseEntity<SolicitudAprobacionProveedor> consultarMiSolicitud(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.consultarUltimaSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudAprobacionProveedor>> listar(
            @RequestParam(required = false) EstadoSolicitud estado) {
        return ResponseEntity.ok(useCase.listar(estado));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudAprobacionProveedor> aprobar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        SolicitudAprobacionProveedor aprobada = useCase.aprobar(admin.getIdUsuario(), id);
        return ResponseEntity.ok(aprobada);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudAprobacionProveedor> rechazar(
            @PathVariable Long id,
            @RequestBody RechazoRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        SolicitudAprobacionProveedor rechazada = useCase.rechazar(admin.getIdUsuario(), id, request.motivo());
        return ResponseEntity.ok(rechazada);
    }

    public record RechazoRequest(String motivo) {}
}
