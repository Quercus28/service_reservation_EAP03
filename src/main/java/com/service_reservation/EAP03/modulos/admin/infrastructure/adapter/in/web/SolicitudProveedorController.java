package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.ActualizarProveedorPerfilComando;
import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Administración - solicitudes de proveedor", description = "Solicitud, consulta y revisión administrativa de la aprobación de proveedores.")
public class SolicitudProveedorController {

    private final GestionarSolicitudesProveedorUseCase useCase;

    public SolicitudProveedorController(GestionarSolicitudesProveedorUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/proveedor")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(summary = "Solicitar aprobación como proveedor", description = "Registra una solicitud de aprobación para el proveedor autenticado. El identificador se obtiene del JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud registrada."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "La operación está disponible para proveedores.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolicitudAprobacionProveedor> solicitarAprobacion(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.registrarSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @PostMapping("/proveedor/reenviar")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(summary = "Reenviar solicitud de aprobación", description = "Solicita nuevamente la aprobación del proveedor autenticado según el estado actual de su solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud reenviada."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "La operación está disponible para proveedores.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolicitudAprobacionProveedor> reenviarSolicitud(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.reenviarSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/proveedor/me")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(summary = "Consultar mi solicitud de proveedor", description = "Obtiene la solicitud más reciente asociada al proveedor autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud consultada."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "La operación está disponible para proveedores.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolicitudAprobacionProveedor> consultarMiSolicitud(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal) {
        SolicitudAprobacionProveedor solicitud = useCase.consultarUltimaSolicitud(principal.getIdUsuario());
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes de proveedor", description = "Lista las solicitudes de aprobación y permite filtrar opcionalmente por estado. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes consultadas."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<SolicitudAprobacionProveedor>> listar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Estado opcional por el que se filtran las solicitudes.")
            @RequestParam(required = false) EstadoSolicitud estado) {
        return ResponseEntity.ok(useCase.listar(estado));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar una solicitud de proveedor", description = "Aprueba la solicitud indicada y registra al administrador autenticado como responsable de la decisión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aprobada."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No se encontró la solicitud.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<SolicitudAprobacionProveedor> aprobar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Identificador de la solicitud.", example = "42")
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        SolicitudAprobacionProveedor aprobada = useCase.aprobar(admin.getIdUsuario(), id);
        return ResponseEntity.ok(aprobada);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar una solicitud de proveedor", description = "Rechaza la solicitud indicada. El motivo es obligatorio y debe tener entre 3 y 500 caracteres.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada."),
            @ApiResponse(responseCode = "400", description = "El motivo no cumple las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No se encontró la solicitud.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Motivo del rechazo (entre 3 y 500 caracteres).",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "motivo": "La documentación presentada está incompleta"
                    }
                    """))
    )
    public ResponseEntity<SolicitudAprobacionProveedor> rechazar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Identificador de la solicitud.", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody RechazoRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        SolicitudAprobacionProveedor rechazada = useCase.rechazar(admin.getIdUsuario(), id, request.motivo());
        return ResponseEntity.ok(rechazada);
    }

    @GetMapping("/proveedor/{idUsuario}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar el perfil de un proveedor", description = "Obtiene los datos del perfil del proveedor correspondiente al usuario indicado. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil consultado."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No se encontró el proveedor.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ProveedorPerfilDTO> consultarPerfilProveedor(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(useCase.consultarPerfilProveedor(idUsuario));
    }

    @PutMapping("/proveedor/{idUsuario}/perfil")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar el perfil de un proveedor", description = "Actualiza la razón social y los datos de contacto del proveedor indicado. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado."),
            @ApiResponse(responseCode = "400", description = "Uno o más campos no cumplen las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No se encontró el proveedor.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del perfil que se actualizarán.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "razonSocial": "Servicios Integrales SAS",
                      "nitRut": "900123456-7",
                      "telefono": "+573001234567"
                    }
                    """))
    )
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
