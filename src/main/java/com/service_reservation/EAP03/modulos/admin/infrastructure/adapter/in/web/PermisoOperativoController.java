package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.*;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarPermisosOperativosUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permisos")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración - permisos operativos", description = "Revocación, restauración y consulta de permisos de usuarios.")
public class PermisoOperativoController {

    private final GestionarPermisosOperativosUseCase useCase;

    public PermisoOperativoController(GestionarPermisosOperativosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/revocar")
    @Operation(summary = "Revocar un permiso operativo", description = "Revoca el permiso indicado y registra al administrador autenticado como responsable de la operación. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permiso revocado."),
            @ApiResponse(responseCode = "400", description = "El usuario, rol o motivo no cumplen las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario objetivo, rol operativo y motivo de revocación.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "idUsuario": 17,
                      "rol": "PROVEEDOR",
                      "motivo": "Incumplimiento de las condiciones del servicio"
                    }
                    """))
    )
    public ResponseEntity<RevocacionPermiso> revocar(
            @Valid @RequestBody RevocarRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        RevocarPermisoComando comando = new RevocarPermisoComando(
                admin.getIdUsuario(),
                request.idUsuario(),
                request.rol(),
                request.motivo()
        );
        return ResponseEntity.ok(useCase.revocar(comando));
    }

    @PostMapping("/restaurar")
    @Operation(summary = "Restaurar un permiso operativo", description = "Restaura el permiso indicado. El motivo es opcional; requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permiso restaurado; la respuesta no contiene cuerpo."),
            @ApiResponse(responseCode = "400", description = "El usuario, rol o motivo no cumplen las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Usuario objetivo, rol y motivo opcional de restauración.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "idUsuario": 17,
                      "rol": "PROVEEDOR",
                      "motivo": "Verificación completada"
                    }
                    """))
    )
    public ResponseEntity<Void> restaurar(
            @Valid @RequestBody RestaurarRequest request,
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal admin) {
        RestaurarPermisoComando comando = new RestaurarPermisoComando(
                admin.getIdUsuario(),
                request.idUsuario(),
                request.rol(),
                request.motivo()
        );
        useCase.restaurar(comando);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Consultar permisos de un usuario", description = "Devuelve los permisos operativos registrados para el usuario indicado. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permisos consultados."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<PermisoOperativo>> consultarPermisos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(useCase.consultarPermisos(idUsuario));
    }

    @GetMapping("/revocaciones")
    @Operation(summary = "Listar revocaciones", description = "Lista las revocaciones de permisos y permite filtrar opcionalmente por estado. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Revocaciones consultadas."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<RevocacionPermiso>> listarRevocaciones(
            @io.swagger.v3.oas.annotations.Parameter(description = "Estado opcional por el que se filtran las revocaciones.")
            @RequestParam(required = false) EstadoRevocacion estado) {
        return ResponseEntity.ok(useCase.listarRevocaciones(estado));
    }

    public record RevocarRequest(
            @NotNull(message = "El idUsuario es obligatorio")
            Long idUsuario,

            @NotNull(message = "El rol es obligatorio")
            RolOperativo rol,

            @NotBlank(message = "El motivo es obligatorio")
            @Size(min = 3, max = 500, message = "El motivo debe tener entre 3 y 500 caracteres")
            @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,;:_\\-()¿?¡!/]+$",
                    message = "El motivo contiene caracteres no permitidos o secuencias sospechosas")
            String motivo
    ) {}

    public record RestaurarRequest(
            @NotNull(message = "El idUsuario es obligatorio")
            Long idUsuario,

            @NotNull(message = "El rol es obligatorio")
            RolOperativo rol,

            @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
            @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s.,;:_\\-()¿?¡!/]*$",
                    message = "El motivo contiene caracteres no permitidos o secuencias sospechosas")
            String motivo
    ) {}
}
