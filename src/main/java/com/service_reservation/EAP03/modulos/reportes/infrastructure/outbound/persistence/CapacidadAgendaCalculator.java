package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.AgendaJpaEntity;
import com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.persistence.entity.HorarioJpaEntity;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Helper de infraestructura que calcula los minutos de capacidad disponible
 * de una agenda en el período solicitado.
 *
 * Algoritmo:
 *  1. Cuenta cuántas veces aparece cada DíaDeLaSemana en [startDate, endDate].
 *  2. Para cada horario de la agenda, multiplica la duración del bloque por
 *     el número de ocurrencias de ese día en el período.
 *  3. Suma todos los bloques → total de minutos disponibles.
 *
 * Esta clase no tiene estado ni anotaciones de Spring; se usa como utilidad
 * estática dentro del adaptador.
 */
class CapacidadAgendaCalculator {

    private CapacidadAgendaCalculator() { }

    /**
     * Calcula los minutos de capacidad ofertada por la agenda en el período.
     *
     * @param agenda  entidad JPA con sus horarios cargados
     * @param periodo rango de fechas del reporte
     * @return minutos totales disponibles; 0 si no hay horarios o la agenda está inactiva
     */
    static long calcularMinutosDisponibles(AgendaJpaEntity agenda, PeriodoReporte periodo) {
        if (!agenda.isActiva() || agenda.getHorarios() == null || agenda.getHorarios().isEmpty()) {
            return 0L;
        }

        Map<DiaSemana, Long> ocurrenciasPorDia = contarOcurrencias(periodo);
        long totalMinutos = 0L;

        for (HorarioJpaEntity horario : agenda.getHorarios()) {
            long ocurrencias = ocurrenciasPorDia.getOrDefault(horario.getDiaSemana(), 0L);
            long minutosBloqueHorario = ChronoUnit.MINUTES.between(
                    horario.getHoraInicio(), horario.getHoraFin());
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
