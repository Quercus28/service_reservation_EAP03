package com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.controller;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import com.service_reservation.EAP03.modulos.availability.domain.ports.in.ConsultarDisponibilidadServicioUseCase;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.BloqueDisponibilidadDTO;
import com.service_reservation.EAP03.modulos.availability.infrastructure.inbound.dto.DisponibilidadServicioResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/servicios")
public class AvailabilityController {

    private final ConsultarDisponibilidadServicioUseCase consultarDisponibilidadServicioUseCase;

    public AvailabilityController(ConsultarDisponibilidadServicioUseCase consultarDisponibilidadServicioUseCase) {
        this.consultarDisponibilidadServicioUseCase = consultarDisponibilidadServicioUseCase;
    }

    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisponibilidadServicioResponseDTO> consultarDisponibilidad(
            @PathVariable("id") Integer id,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
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
                bloques != null ? bloques : Collections.emptyList(),
                slotsLibres.isEmpty() ? DisponibilidadServicioResponseDTO.MENSAJE_SIN_ESPACIOS : null
        );

        return ResponseEntity.ok(respuesta);
    }
}
