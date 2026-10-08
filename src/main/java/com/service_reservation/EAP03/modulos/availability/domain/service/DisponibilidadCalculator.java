package com.service_reservation.EAP03.modulos.availability.domain.service;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Domain Service: Motor de cálculo puro para resolver la disponibilidad de horarios.
 * Totalmente desacoplado de frameworks (Spring, JPA, etc.).
 */
public class DisponibilidadCalculator {

    /**
     * Calcula los intervalos disponibles para un servicio dentro de un rango de fechas.
     *
     * @param dateRange       Rango de fechas a consultar.
     * @param horariosSemanales Horarios semanales del proveedor configurados en la agenda.
     * @param reservasOcupadas Lista de franjas ya ocupadas o bloqueadas (reservas existentes).
     * @param duracionMinutos Duración requerida para cada turno/servicio en minutos.
     * @return Lista de TimeSlots disponibles ordenados cronológicamente.
     */
    public List<TimeSlot> calcularDisponibilidad(
            DateRange dateRange,
            List<HorarioSemanal> horariosSemanales,
            List<TimeSlot> reservasOcupadas,
            int duracionMinutos) {

        Objects.requireNonNull(dateRange, "El rango de fechas no puede ser nulo");
        if (duracionMinutos <= 0) {
            throw new DisponibilidadInvalidaException("La duración del turno debe ser mayor a 0 minutos");
        }

        if (horariosSemanales == null || horariosSemanales.isEmpty()) {
            return Collections.emptyList();
        }

        Map<DiaSemana, List<HorarioSemanal>> horariosPorDia = horariosSemanales.stream()
                .collect(Collectors.groupingBy(HorarioSemanal::diaSemana));

        List<TimeSlot> franjasDisponibles = new ArrayList<>();

        LocalDate fechaActual = dateRange.startDate();
        LocalDate fechaFin = dateRange.endDate();

        while (!fechaActual.isAfter(fechaFin)) {
            DiaSemana diaSemana = mapearDiaSemana(fechaActual.getDayOfWeek());
            List<HorarioSemanal> horariosDelDia = horariosPorDia.getOrDefault(diaSemana, Collections.emptyList());

            for (HorarioSemanal horario : horariosDelDia) {
                LocalDateTime inicioVentana = fechaActual.atTime(horario.horaInicio());
                LocalDateTime finVentana = fechaActual.atTime(horario.horaFin());

                List<TimeSlot> franjasVentana = segmentarYFiltrarOcupados(
                        inicioVentana,
                        finVentana,
                        reservasOcupadas,
                        duracionMinutos
                );
                franjasDisponibles.addAll(franjasVentana);
            }

            fechaActual = fechaActual.plusDays(1);
        }

        Collections.sort(franjasDisponibles);
        return franjasDisponibles;
    }

    private List<TimeSlot> segmentarYFiltrarOcupados(
            LocalDateTime inicioVentana,
            LocalDateTime finVentana,
            List<TimeSlot> ocupados,
            int duracionMinutos) {

        List<TimeSlot> disponibles = new ArrayList<>();
        LocalDateTime cursor = inicioVentana;

        while (!cursor.plusMinutes(duracionMinutos).isAfter(finVentana)) {
            TimeSlot slotCandidato = new TimeSlot(cursor, cursor.plusMinutes(duracionMinutos));

            boolean estaOcupado = ocupados != null && ocupados.stream()
                    .anyMatch(slotCandidato::seSolapaCon);

            if (!estaOcupado) {
                disponibles.add(slotCandidato);
            }

            cursor = cursor.plusMinutes(duracionMinutos);
        }

        return disponibles;
    }

    private DiaSemana mapearDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
