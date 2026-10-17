package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.*;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.*;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.config.RecursoSecurity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

// /disponibilidad no choca con GET /{id} de RecursoController: Spring prioriza el
// segmento literal sobre la variable de ruta al elegir el handler.
@RestController
@RequestMapping("/api/v1/recursos")
public class DisponibilidadRecursoController {

    // El proveedor además tiene que tener sus permisos activos: si el administrador se los
    // quitó (o todavía no aprobó su cuenta), ya no puede tocar horarios ni bloqueos.
    private static final String PROPIETARIO_O_ADMIN =
            "hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')"
                    + " and @recursoSecurity.esPropietario(authentication, #id))";

    private final RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase;
    private final ListarHorariosRecursoUseCase listarHorariosRecursoUseCase;
    private final EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase;
    private final RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase;
    private final ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase;
    private final EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase;
    private final ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase;
    private final RecursoSecurity recursoSecurity;

    public DisponibilidadRecursoController(RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase,
                                           ListarHorariosRecursoUseCase listarHorariosRecursoUseCase,
                                           EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase,
                                           RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase,
                                           ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase,
                                           EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase,
                                           ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase,
                                           RecursoSecurity recursoSecurity) {
        this.registrarHorarioRecursoUseCase = registrarHorarioRecursoUseCase;
        this.listarHorariosRecursoUseCase = listarHorariosRecursoUseCase;
        this.eliminarHorarioRecursoUseCase = eliminarHorarioRecursoUseCase;
        this.registrarBloqueoRecursoUseCase = registrarBloqueoRecursoUseCase;
        this.listarBloqueosRecursoUseCase = listarBloqueosRecursoUseCase;
        this.eliminarBloqueoRecursoUseCase = eliminarBloqueoRecursoUseCase;
        this.consultarDisponibilidadProveedorUseCase = consultarDisponibilidadProveedorUseCase;
        this.recursoSecurity = recursoSecurity;
    }

    @PostMapping("/{id}/horarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public HorarioRecursoResponseDTO registrarHorario(@PathVariable Integer id,
                                                      @Valid @RequestBody RegistrarHorarioRequestDTO request) {
        HorarioRecurso horario = registrarHorarioRecursoUseCase.ejecutar(new RegistrarHorarioComando(
                id,
                DiaSemana.valueOf(request.diaSemana()),
                LocalTime.parse(request.horaInicio()),
                LocalTime.parse(request.horaFin())
        ));
        return HorarioRecursoResponseDTO.desde(horario);
    }

    @GetMapping("/{id}/horarios")
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public List<HorarioRecursoResponseDTO> listarHorarios(@PathVariable Integer id) {
        return listarHorariosRecursoUseCase.ejecutar(id).stream()
                .map(HorarioRecursoResponseDTO::desde)
                .toList();
    }

    @DeleteMapping("/{id}/horarios/{idHorario}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public void eliminarHorario(@PathVariable Integer id, @PathVariable Integer idHorario) {
        eliminarHorarioRecursoUseCase.ejecutar(id, idHorario);
    }

    @PostMapping("/{id}/bloqueos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public BloqueoRecursoResponseDTO registrarBloqueo(@PathVariable Integer id,
                                                      @Valid @RequestBody RegistrarBloqueoRequestDTO request) {
        BloqueoRecurso bloqueo = registrarBloqueoRecursoUseCase.ejecutar(new RegistrarBloqueoComando(
                id,
                request.fechaInicio(),
                request.fechaFin(),
                request.motivo()
        ));
        return BloqueoRecursoResponseDTO.desde(bloqueo);
    }

    @GetMapping("/{id}/bloqueos")
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public PaginaResponseDTO<BloqueoRecursoResponseDTO> listarBloqueos(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {

        ResultadoPaginado<BloqueoRecurso> resultado = listarBloqueosRecursoUseCase.ejecutar(id, pagina, tamano);

        return new PaginaResponseDTO<>(
                resultado.getContenido().stream().map(BloqueoRecursoResponseDTO::desde).toList(),
                resultado.getPagina(),
                resultado.getTamano(),
                resultado.getTotalElementos(),
                resultado.getTotalPaginas()
        );
    }

    @DeleteMapping("/{id}/bloqueos/{idBloqueo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public void eliminarBloqueo(@PathVariable Integer id, @PathVariable Integer idBloqueo) {
        eliminarBloqueoRecursoUseCase.ejecutar(id, idBloqueo);
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
    public PaginaResponseDTO<RecursoDisponibilidadResponseDTO> consultarDisponibilidadProveedor(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication authentication) {

        Integer idProveedor = recursoSecurity.idProveedorDe(authentication)
                .orElseThrow(() -> new DatosInvalidosException("El usuario autenticado no tiene un perfil de proveedor asociado"));

        ResultadoPaginado<RecursoConHorarios> resultado =
                consultarDisponibilidadProveedorUseCase.ejecutar(idProveedor, pagina, tamano);

        return new PaginaResponseDTO<>(
                resultado.getContenido().stream().map(RecursoDisponibilidadResponseDTO::desde).toList(),
                resultado.getPagina(),
                resultado.getTamano(),
                resultado.getTotalElementos(),
                resultado.getTotalPaginas()
        );
    }
}
