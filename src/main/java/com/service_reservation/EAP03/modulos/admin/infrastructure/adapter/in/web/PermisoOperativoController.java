package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.*;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarPermisosOperativosUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
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
public class PermisoOperativoController {

    private final GestionarPermisosOperativosUseCase useCase;

    public PermisoOperativoController(GestionarPermisosOperativosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/revocar")
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
    public ResponseEntity<List<PermisoOperativo>> consultarPermisos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(useCase.consultarPermisos(idUsuario));
    }

    @GetMapping("/revocaciones")
    public ResponseEntity<List<RevocacionPermiso>> listarRevocaciones(
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
