package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CrearReservaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Creación y cancelación de reservas por el cliente autenticado.")
public class ReservaController {

    private final CrearReservaUseCase crearReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;

    public ReservaController(CrearReservaUseCase crearReservaUseCase,
                             CancelarReservaUseCase cancelarReservaUseCase) {
        this.crearReservaUseCase = crearReservaUseCase;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
    }

    // Además del rol, el cliente tiene que tener sus permisos activos: si el administrador
    // se los quitó, ya no puede reservar ni cancelar.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE') and @permisoOperativo.activo(authentication, 'CLIENTE')")
    @Operation(
            summary = "Crear una reserva",
            description = "Reserva un servicio para el cliente autenticado. La fecha de fin se determina según la duración del servicio; el usuario se obtiene del JWT y no del cuerpo. Requiere rol CLIENTE y permiso operativo activo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservaResponseDTO.class), examples = @ExampleObject(value = """
                    {
                      "id": 87,
                      "idServicio": 12,
                      "fechaInicio": "2026-11-02T09:00:00",
                      "fechaFin": "2026-11-02T10:00:00",
                      "estado": "RESERVADO",
                      "notas": "Acceso por la entrada principal"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Los datos son inválidos, faltan campos obligatorios o se repiten recursos opcionales.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol CLIENTE o su permiso operativo está inactivo.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No existe el servicio solicitado.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "El horario o alguno de los recursos solicitados ya no está disponible.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Servicio, fecha y hora de inicio, notas opcionales y recursos adicionales opcionales.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "idServicio": 12,
                      "fechaInicio": "2026-11-02T09:00:00",
                      "notas": "Acceso por la entrada principal",
                      "recursosOpcionales": [3]
                    }
                    """))
    )
    public ReservaResponseDTO crear(@Valid @RequestBody CrearReservaRequestDTO request,
                                    Authentication authentication) {
        Reserva reserva = crearReservaUseCase.ejecutar(new CrearReservaComando(
                idUsuarioDe(authentication),
                request.idServicio(),
                request.fechaInicio(),
                request.notas(),
                request.recursosOpcionales()
        ));
        return ReservaResponseDTO.desde(reserva);
    }

    // El cuerpo es opcional: el cliente puede cancelar sin dar motivo.
    // Si {id} no es un número, Spring responde 400 antes de llegar aquí.
    @PostMapping("/{id}/cancelacion")
    @PreAuthorize("hasRole('CLIENTE') and @permisoOperativo.activo(authentication, 'CLIENTE')")
    @Operation(
            summary = "Cancelar una reserva",
            description = "Cancela una reserva del cliente autenticado. El motivo es opcional y no puede superar 255 caracteres. Requiere rol CLIENTE y permiso operativo activo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reserva cancelada."),
            @ApiResponse(responseCode = "400", description = "El identificador o el motivo no son válidos.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no puede cancelar esta reserva o su permiso operativo está inactivo.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "La reserva no existe.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "La reserva no se puede cancelar o está fuera del plazo permitido.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Cuerpo opcional. Si se envía, puede incluir un motivo de cancelación.",
            required = false,
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "motivo": "Cambio de planes"
                    }
                    """))
    )
    public CancelacionReservaResponseDTO cancelar(@PathVariable Integer id,
                                                  @Valid @RequestBody(required = false) CancelarReservaRequestDTO request,
                                                  Authentication authentication) {
        Reserva reserva = cancelarReservaUseCase.ejecutar(new CancelarReservaComando(
                idUsuarioDe(authentication),
                id,
                request != null ? request.motivo() : null
        ));
        return CancelacionReservaResponseDTO.desde(reserva);
    }

    // Se toma el usuario que inició sesión. Si no se puede identificar, el caso de uso
    // responde 403 (no es cliente).
    private static Long idUsuarioDe(Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal
                ? principal.getIdUsuario()
                : null;
    }
}
