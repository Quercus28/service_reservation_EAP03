package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.ActualizarProveedorPerfilComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
            @Valid @RequestBody RechazoRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        SolicitudAprobacionProveedor rechazada = useCase.rechazar(admin.getIdUsuario(), id, request.motivo());
        return ResponseEntity.ok(rechazada);
    }

    @GetMapping("/proveedor/{idUsuario}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProveedorPerfilDTO> consultarPerfilProveedor(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(useCase.consultarPerfilProveedor(idUsuario));
    }

    @PutMapping("/proveedor/{idUsuario}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProveedorPerfilDTO> actualizarPerfilProveedor(
            @PathVariable Long idUsuario,
            @Valid @RequestBody ActualizarPerfilProveedorRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        ActualizarProveedorPerfilComando comando = new ActualizarProveedorPerfilComando(
                admin.getIdUsuario(),
                idUsuario,
                request.razonSocial(),
                request.nitRut(),
                request.telefono()
        );
        return ResponseEntity.ok(useCase.actualizarPerfilProveedor(comando));
    }

    public record RechazoRequest(
            @NotBlank(message = "El motivo es obligatorio")
            @Size(min = 3, max = 500, message = "El motivo debe tener entre 3 y 500 caracteres")
            @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,;:_\\-()¿?¡!/]+$",
                    message = "El motivo contiene caracteres no permitidos o secuencias sospechosas")
            String motivo
    ) {}

    public record ActualizarPerfilProveedorRequest(
            @NotBlank(message = "La razón social es obligatoria")
            @Size(max = 255, message = "La razón social no puede superar 255 caracteres")
            @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,;:_\\-()¿?¡!/]+$",
                    message = "La razón social contiene caracteres inválidos")
            String razonSocial,

            @Size(max = 50, message = "El NIT/RUT no puede superar 50 caracteres")
            @Pattern(regexp = "^[a-zA-Z0-9\\-\\.]*$",
                    message = "El NIT/RUT contiene caracteres inválidos")
            String nitRut,

            @Size(max = 50, message = "El teléfono no puede superar 50 caracteres")
            @Pattern(regexp = "^[0-9+\\-\\s()]*$",
                    message = "El teléfono contiene caracteres inválidos")
            String telefono
    ) {}
}
