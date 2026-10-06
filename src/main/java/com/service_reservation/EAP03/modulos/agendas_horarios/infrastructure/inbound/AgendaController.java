package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agendas")
@PreAuthorize("hasRole('PROVEEDOR')")
public class AgendaController {
    private final IGestionAgendas gestionAgendas;
    private final IGestionHorarios gestionHorarios;
    private final IDisponibilidad disponibilidad;
    private final ConsultarProveedorUseCase consultarProveedor;

    public AgendaController(IGestionAgendas gestionAgendas, IGestionHorarios gestionHorarios, IDisponibilidad disponibilidad,ConsultarProveedorUseCase consultarProveedor) {
        this.gestionAgendas = gestionAgendas;
        this.gestionHorarios = gestionHorarios;
        this.disponibilidad = disponibilidad;
        this.consultarProveedor = consultarProveedor;
    }

    @PostMapping
    public ResponseEntity<AgendaResponseDTO> crearAgenda(
            @Valid @RequestBody CrearAgendaRequestDTO request,
            Authentication authentication) {
        Integer proveedorId = proveedorId(authentication);
        Agenda agenda = gestionAgendas.crearAgenda(proveedorId, request.servicioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendaResponseDTO.from(agenda));
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AgendaResponseDTO> consultarAgenda(
            @PathVariable Long agendaId,
            Authentication authentication) {
        Agenda agenda = gestionAgendas.consultarAgenda(agendaId, proveedorId(authentication));
        return ResponseEntity.ok(AgendaResponseDTO.from(agenda));
    }

    @PostMapping("/{agendaId}/horarios")
    public ResponseEntity<AgendaResponseDTO> agregarHorario(
            @PathVariable Long agendaId,
            @Valid @RequestBody AgendaHorarioRequestDTO request,
            Authentication authentication) {
        Horario horario = new Horario(request.diaSemana(), request.horaInicioLocal(), request.horaFinLocal());
        Agenda agenda = gestionHorarios.agregarHorario(agendaId, proveedorId(authentication), horario);
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendaResponseDTO.from(agenda));
    }

    @PutMapping("/{agendaId}/horarios/{horarioId}")
    public ResponseEntity<AgendaResponseDTO> modificarHorario(
            @PathVariable Long agendaId,
            @PathVariable Long horarioId,
            @Valid @RequestBody AgendaHorarioRequestDTO request,
            Authentication authentication) {
        Horario horario = new Horario(request.diaSemana(), request.horaInicioLocal(), request.horaFinLocal());
        Agenda agenda = gestionHorarios.modificarHorario(agendaId, proveedorId(authentication), horarioId, horario);
        return ResponseEntity.ok(AgendaResponseDTO.from(agenda));
    }

    @GetMapping("/servicios/{servicioId}/disponibilidad")
    public ResponseEntity<List<Horario>> consultarDisponibilidad(
            @PathVariable Integer servicioId,
            Authentication authentication) {
        return ResponseEntity.ok(
                disponibilidad.consultarHorariosDisponibles(
                        proveedorId(authentication), servicioId));
    }

    private Integer proveedorId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
            throw new DatosAgendaInvalidosException("El usuario autenticado es obligatorio.");
        }
        Long idUsuario = principal.getIdUsuario();
        return consultarProveedor.buscarIdProveedorPorUsuario(idUsuario)
                .orElseThrow(() -> new DatosAgendaInvalidosException(
                        "El usuario autenticado no corresponde a un proveedor."));
    }
}
