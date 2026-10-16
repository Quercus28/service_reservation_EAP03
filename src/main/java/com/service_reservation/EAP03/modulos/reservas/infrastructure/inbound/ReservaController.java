package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CrearReservaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final CrearReservaUseCase crearReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;

    public ReservaController(CrearReservaUseCase crearReservaUseCase,
                             CancelarReservaUseCase cancelarReservaUseCase) {
        this.crearReservaUseCase = crearReservaUseCase;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public ReservaResponseDTO crear(@Valid @RequestBody CrearReservaRequestDTO request,
                                    Authentication authentication) {
        Reserva reserva = crearReservaUseCase.ejecutar(new CrearReservaComando(
                idUsuarioDe(authentication),
                request.idServicio(),
                request.fechaInicio(),
                request.notas()
        ));
        return ReservaResponseDTO.desde(reserva);
    }

    // El cuerpo es opcional: el cliente puede cancelar sin dar motivo.
    // Si {id} no es un número, Spring responde 400 antes de llegar aquí.
    @PostMapping("/{id}/cancelacion")
    @PreAuthorize("hasRole('CLIENTE')")
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
