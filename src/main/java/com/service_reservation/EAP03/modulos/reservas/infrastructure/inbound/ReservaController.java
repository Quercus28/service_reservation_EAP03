package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
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

    public ReservaController(CrearReservaUseCase crearReservaUseCase) {
        this.crearReservaUseCase = crearReservaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public ReservaResponseDTO crear(@Valid @RequestBody CrearReservaRequestDTO request,
                                    Authentication authentication) {
        // Se toma el usuario que inició sesión. Si no se puede identificar, el caso de uso
        // responde 403 (no es cliente).
        Long idUsuario = authentication != null
                && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal
                ? principal.getIdUsuario()
                : null;

        Reserva reserva = crearReservaUseCase.ejecutar(new CrearReservaComando(
                idUsuario,
                request.idServicio(),
                request.fechaInicio(),
                request.notas()
        ));
        return ReservaResponseDTO.desde(reserva);
    }
}
