package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.controller;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.in.ConsultarDisponibilidadServicioUseCase;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.BloqueDisponibilidadDTO;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.DisponibilidadServicioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/servicios")
@Tag(name = "Disponibilidad", description = "Consulta de horarios libres para los servicios publicados.")
public class AvailabilityController {

    private final ConsultarDisponibilidadServicioUseCase consultarDisponibilidadServicioUseCase;

    public AvailabilityController(ConsultarDisponibilidadServicioUseCase consultarDisponibilidadServicioUseCase) {
        this.consultarDisponibilidadServicioUseCase = consultarDisponibilidadServicioUseCase;
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Consultar disponibilidad de un servicio",
            description = "Devuelve las franjas horarias disponibles agrupadas por fecha dentro del intervalo indicado. Ambas fechas son obligatorias, usan formato ISO YYYY-MM-DD y endDate no puede ser anterior a startDate. Requiere JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad consultada; la lista puede estar vacía si no hay franjas libres.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DisponibilidadServicioResponseDTO.class), examples = @ExampleObject(value = """
                    {
                      "servicioId": 12,
                      "totalEspaciosDisponibles": 1,
                      "disponibilidad": [
                        {
                          "fecha": "2026-11-02",
                          "franjas": [
                            {
                              "inicio": "09:00:00",
                              "fin": "10:00:00"
                            }
                          ]
                        }
                      ],
                      "mensaje": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Fechas ausentes, con formato inválido o intervalo incoherente.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para consultar la disponibilidad.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No existe el servicio indicado.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DisponibilidadServicioResponseDTO> consultarDisponibilidad(
            @io.swagger.v3.oas.annotations.Parameter(description = "Identificador numérico del servicio.", example = "12")
            @PathVariable("id") Integer id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Fecha inicial del rango, inclusive, en formato ISO YYYY-MM-DD.", example = "2026-11-02", required = true)
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @io.swagger.v3.oas.annotations.Parameter(description = "Fecha final del rango, inclusive, en formato ISO YYYY-MM-DD.", example = "2026-11-05", required = true)
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (id == null) {
            throw new DisponibilidadInvalidaException("El parámetro 'id' del servicio es obligatorio");
        }
        if (startDate == null) {
            throw new DisponibilidadInvalidaException("El parámetro 'startDate' es obligatorio y debe tener formato ISO YYYY-MM-DD");
        }
        if (endDate == null) {
            throw new DisponibilidadInvalidaException("El parámetro 'endDate' es obligatorio y debe tener formato ISO YYYY-MM-DD");
        }
        if (endDate.isBefore(startDate)) {
            throw new DisponibilidadInvalidaException("El parámetro 'endDate' no puede ser anterior a 'startDate'");
        }

        List<TimeSlot> slotsLibres = consultarDisponibilidadServicioUseCase.ejecutar(id, startDate, endDate);

        // Agrupar resultados por fecha (bloques por día)
        Map<LocalDate, List<TimeSlot>> porFecha = slotsLibres.stream()
                .collect(Collectors.groupingBy(slot -> slot.start().toLocalDate()));

        List<BloqueDisponibilidadDTO> bloques = porFecha.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new BloqueDisponibilidadDTO(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(TimeSlot::start))
                                .map(BloqueDisponibilidadDTO.FranjaHorariaDTO::from)
                                .toList()
                ))
                .toList();

        DisponibilidadServicioResponseDTO respuesta = new DisponibilidadServicioResponseDTO(
                id,
                slotsLibres.size(),
                bloques,
                slotsLibres.isEmpty() ? DisponibilidadServicioResponseDTO.MENSAJE_SIN_ESPACIOS : null
        );

        return ResponseEntity.ok(respuesta);
    }
}
