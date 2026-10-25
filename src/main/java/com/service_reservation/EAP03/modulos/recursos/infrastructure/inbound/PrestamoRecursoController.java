package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound.ReglasAccesoRecurso.PROPIETARIO_O_ADMIN;

@RestController
@RequestMapping("/api/v1/recursos")
public class PrestamoRecursoController {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;

    public PrestamoRecursoController(ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                                     RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase) {
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.registrarPrestamoRecursoUseCase = registrarPrestamoRecursoUseCase;
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("isAuthenticated()")
    public DisponibilidadResponseDTO consultarDisponibilidad(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        return DisponibilidadResponseDTO.desde(
                consultarDisponibilidadUseCase.ejecutar(id, desde, hasta));
    }

    @PostMapping("/{id}/prestamos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public RecursoPrestadoResponseDTO registrarPrestamo(
            @PathVariable Integer id,
            @Valid @RequestBody RegistrarPrestamoRequestDTO request) {

        RecursoPrestado prestamo = registrarPrestamoRecursoUseCase.ejecutar(new RegistrarPrestamoComando(
                id,
                request.idServicioPrestado(),
                request.cantidad(),
                request.fechaInicio(),
                request.fechaFin(),
                request.precioTotal()
        ));
        return RecursoPrestadoResponseDTO.desde(prestamo);
    }
}
