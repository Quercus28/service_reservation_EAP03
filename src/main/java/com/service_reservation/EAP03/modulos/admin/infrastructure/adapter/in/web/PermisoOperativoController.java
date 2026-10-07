package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.admin.domain.model.*;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarPermisosOperativosUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
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
            @RequestBody RevocarRequest request,
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
            @RequestBody RestaurarRequest request,
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

    public record RevocarRequest(Long idUsuario, RolOperativo rol, String motivo) {}
    public record RestaurarRequest(Long idUsuario, RolOperativo rol, String motivo) {}
}
