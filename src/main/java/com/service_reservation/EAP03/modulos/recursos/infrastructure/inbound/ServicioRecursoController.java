package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.AsociarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.AsociarRecursoAServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarRecursosDeServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.QuitarRecursoDeServicioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios/{idServicio}/recursos")
public class ServicioRecursoController {

    // Solo el dueño del servicio (con sus permisos activos) o el administrador. Que el
    // recurso también sea de ese proveedor lo revisa el caso de uso al asociar.
    private static final String PROPIETARIO_O_ADMIN =
            "hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')"
                    + " and @servicioSecurity.esPropietario(authentication, #idServicio))";

    private final AsociarRecursoAServicioUseCase asociarRecursoAServicioUseCase;
    private final ListarRecursosDeServicioUseCase listarRecursosDeServicioUseCase;
    private final QuitarRecursoDeServicioUseCase quitarRecursoDeServicioUseCase;

    public ServicioRecursoController(AsociarRecursoAServicioUseCase asociarRecursoAServicioUseCase,
                                     ListarRecursosDeServicioUseCase listarRecursosDeServicioUseCase,
                                     QuitarRecursoDeServicioUseCase quitarRecursoDeServicioUseCase) {
        this.asociarRecursoAServicioUseCase = asociarRecursoAServicioUseCase;
        this.listarRecursosDeServicioUseCase = listarRecursosDeServicioUseCase;
        this.quitarRecursoDeServicioUseCase = quitarRecursoDeServicioUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public ServicioRecursoResponseDTO asociar(@PathVariable Integer idServicio,
                                              @Valid @RequestBody AsociarRecursoRequestDTO request) {
        // Si no mandan cantidad se usa 1, y si no dicen nada de obligatorio se toma como obligatorio.
        ServicioRecurso asociacion = asociarRecursoAServicioUseCase.ejecutar(new AsociarRecursoComando(
                idServicio,
                request.idRecurso(),
                request.cantidad() != null ? request.cantidad() : 1,
                request.obligatorio() == null || request.obligatorio()
        ));
        return ServicioRecursoResponseDTO.desde(asociacion);
    }

    @GetMapping
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public List<ServicioRecursoResponseDTO> listar(@PathVariable Integer idServicio) {
        return listarRecursosDeServicioUseCase.ejecutar(idServicio).stream()
                .map(ServicioRecursoResponseDTO::desde)
                .toList();
    }

    @DeleteMapping("/{idRecurso}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public void quitar(@PathVariable Integer idServicio, @PathVariable Integer idRecurso) {
        quitarRecursoDeServicioUseCase.ejecutar(idServicio, idRecurso);
    }
}
