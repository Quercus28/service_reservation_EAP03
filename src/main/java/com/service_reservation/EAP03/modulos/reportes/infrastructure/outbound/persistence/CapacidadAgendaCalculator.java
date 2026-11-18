package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Helper de cálculo de capacidad disponible para un período.
 * Opera exclusivamente sobre el modelo de dominio público {@link Horario}
 * provisto por el puerto de disponibilidad de agendas.
 *
 * Algoritmo:
 *  1. Cuenta cuántas veces aparece cada DíaDeLaSemana en [startDate, endDate].
 *  2. Para cada horario de la agenda, multiplica la duración del bloque por
 *     el número de ocurrencias de ese día en el período.
 *  3. Suma todos los bloques -> total de minutos disponibles.
 */
public class CapacidadAgendaCalculator {

    private CapacidadAgendaCalculator() { }

    /**
     * Calcula los minutos de capacidad ofertada por la lista de horarios en el período.
     *
     * @param horarios lista de horarios semanales provistos por el puerto de agendas
     * @param periodo  rango de fechas del reporte
     * @return minutos totales disponibles; 0 si no hay horarios
     */
    public static long calcularMinutosDisponibles(List<Horario> horarios, PeriodoReporte periodo) {
        if (horarios == null || horarios.isEmpty()) {
            return 0L;
        }

        Map<DiaSemana, Long> ocurrenciasPorDia = contarOcurrencias(periodo);
        long totalMinutos = 0L;

        for (Horario horario : horarios) {
            long ocurrencias = ocurrenciasPorDia.getOrDefault(horario.diaSemana(), 0L);
            long minutosBloqueHorario = ChronoUnit.MINUTES.between(
                    horario.horaInicio(), horario.horaFin());
            totalMinutos += ocurrencias * minutosBloqueHorario;
        }
        return totalMinutos;
    }

    /**
     * Recorre el rango [startDate, endDate] y cuenta cuántas veces aparece
     * cada día de la semana. Usa EnumMap para acceso O(1).
     */
    private static Map<DiaSemana, Long> contarOcurrencias(PeriodoReporte periodo) {
        Map<DiaSemana, Long> conteo = new EnumMap<>(DiaSemana.class);
        LocalDate cursor = periodo.startDate();

        while (!cursor.isAfter(periodo.endDate())) {
            DiaSemana dia = toDiaSemana(cursor.getDayOfWeek());
            conteo.merge(dia, 1L, Long::sum);
            cursor = cursor.plusDays(1);
        }
        return conteo;
    }

    /** Mapea java.time.DayOfWeek al enum DiaSemana del dominio del proyecto. */
    private static DiaSemana toDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY    -> DiaSemana.LUNES;
            case TUESDAY   -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY  -> DiaSemana.JUEVES;
            case FRIDAY    -> DiaSemana.VIERNES;
            case SATURDAY  -> DiaSemana.SABADO;
            case SUNDAY    -> DiaSemana.DOMINGO;
        };
    }
}
