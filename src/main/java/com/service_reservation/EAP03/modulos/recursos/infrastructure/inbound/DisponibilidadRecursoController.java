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

import static com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound.ReglasAccesoRecurso.PROPIETARIO_O_ADMIN;

// /disponibilidad no choca con GET /{id} de RecursoController: Spring prioriza el
// segmento literal sobre la variable de ruta al elegir el handler.
@RestController
@RequestMapping("/api/v1/recursos")
public class DisponibilidadRecursoController {

    private final RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase;
    private final ListarHorariosRecursoUseCase listarHorariosRecursoUseCase;
    private final EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase;
    private final ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase;
    private final RecursoSecurity recursoSecurity;

    public DisponibilidadRecursoController(RegistrarHorarioRecursoUseCase registrarHorarioRecursoUseCase,
                                           ListarHorariosRecursoUseCase listarHorariosRecursoUseCase,
                                           EliminarHorarioRecursoUseCase eliminarHorarioRecursoUseCase,
                                           ConsultarDisponibilidadProveedorUseCase consultarDisponibilidadProveedorUseCase,
                                           RecursoSecurity recursoSecurity) {
        this.registrarHorarioRecursoUseCase = registrarHorarioRecursoUseCase;
        this.listarHorariosRecursoUseCase = listarHorariosRecursoUseCase;
        this.eliminarHorarioRecursoUseCase = eliminarHorarioRecursoUseCase;
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
