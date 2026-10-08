package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object inmutable que representa una franja de tiempo entre start y end.
 */
public record TimeSlot(LocalDateTime start, LocalDateTime end) implements Comparable<TimeSlot> {

    public TimeSlot {
        Objects.requireNonNull(start, "La fecha/hora de inicio es obligatoria");
        Objects.requireNonNull(end, "La fecha/hora de fin es obligatoria");

        if (!end.isAfter(start)) {
            throw new DisponibilidadInvalidaException("La fecha/hora final debe ser estrictamente posterior a la inicial");
        }
    }

    public boolean seSolapaCon(TimeSlot otro) {
        if (otro == null) {
            return false;
        }
        return this.start.isBefore(otro.end) && this.end.isAfter(otro.start);
    }

    public boolean contiene(TimeSlot otro) {
        if (otro == null) {
            return false;
        }
        return !this.start.isAfter(otro.start) && !this.end.isBefore(otro.end);
    }

    @Override
    public int compareTo(TimeSlot o) {
        int cmp = this.start.compareTo(o.start);
        if (cmp != 0) {
            return cmp;
        }
        return this.end.compareTo(o.end);
    }
}
