package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.application.GestionAgendasService;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {
    private final GestionAgendasService gestionAgendas;
    private final IGestionHorarios gestionHorarios;

    public AgendaController(GestionAgendasService gestionAgendas, IGestionHorarios gestionHorarios) {
        this.gestionAgendas = gestionAgendas;
        this.gestionHorarios = gestionHorarios;
    }

    @PostMapping
    public ResponseEntity<AgendaResponseDTO> crearAgenda(Authentication authentication) {
        Long proveedorId = gestionAgendas.obtenerProveedorId(authentication.getName());
        Agenda agenda = gestionAgendas.crearAgenda(proveedorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendaResponseDTO.from(agenda));
    }

    @GetMapping("/{agendaId}")
    public ResponseEntity<AgendaResponseDTO> consultarAgenda(
            @PathVariable Long agendaId,
            Authentication authentication) {
        Long proveedorId = gestionAgendas.obtenerProveedorId(authentication.getName());
        Agenda agenda = gestionAgendas.consultarAgenda(agendaId, proveedorId);
        return ResponseEntity.ok(AgendaResponseDTO.from(agenda));
    }

    @PostMapping("/{agendaId}/horarios")
    public ResponseEntity<AgendaResponseDTO> agregarHorario(
            @PathVariable Long agendaId,
            @Valid @RequestBody AgendaHorarioRequestDTO request,
            Authentication authentication) {
        Long proveedorId = gestionAgendas.obtenerProveedorId(authentication.getName());
        Horario horario = new Horario(request.diaSemana(), request.horaInicioLocal(), request.horaFinLocal());
        Agenda agenda = gestionHorarios.agregarHorario(agendaId, proveedorId, horario);
        return ResponseEntity.status(HttpStatus.CREATED).body(AgendaResponseDTO.from(agenda));
    }

    @PutMapping("/{agendaId}/horarios")
    public ResponseEntity<AgendaResponseDTO> modificarHorario(
            @PathVariable Long agendaId,
            @Valid @RequestBody ModificarHorarioRequestDTO request,
            Authentication authentication) {
        Long proveedorId = gestionAgendas.obtenerProveedorId(authentication.getName());
        Horario actual = new Horario(request.horarioActual().diaSemana(), request.horarioActual().horaInicioLocal(), request.horarioActual().horaFinLocal());
        Horario nuevo = new Horario(request.nuevoHorario().diaSemana(), request.nuevoHorario().horaInicioLocal(), request.nuevoHorario().horaFinLocal());
        Agenda agenda = gestionHorarios.modificarHorario(agendaId, proveedorId, actual, nuevo);
        return ResponseEntity.ok(AgendaResponseDTO.from(agenda));
    }
}
