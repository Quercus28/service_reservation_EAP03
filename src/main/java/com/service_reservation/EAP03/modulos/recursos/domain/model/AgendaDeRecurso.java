package com.service_reservation.EAP03.modulos.recursos.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

// Todo lo que hace falta para saber si un recurso se puede usar en un rango de tiempo: el
// recurso, sus horarios de atención, sus bloqueos y sus préstamos activos. Quien la arma
// decide qué rango cubren los bloqueos y préstamos que trae; aquí solo se revisa cada rango.
public class AgendaDeRecurso {

    private final Recurso recurso;
    private final List<HorarioRecurso> horarios;
    private final List<BloqueoRecurso> bloqueos;
    private final List<RecursoPrestado> prestamosActivos;

    public AgendaDeRecurso(Recurso recurso, List<HorarioRecurso> horarios,
                           List<BloqueoRecurso> bloqueos, List<RecursoPrestado> prestamosActivos) {
        this.recurso = Objects.requireNonNull(recurso, "recurso es obligatorio");
        this.horarios = horarios == null ? List.of() : horarios;
        this.bloqueos = bloqueos == null ? List.of() : bloqueos;
        this.prestamosActivos = prestamosActivos == null ? List.of() : prestamosActivos;
    }

    // Regla de compatibilidad (HU06): un recurso SIN ningún horario configurado se considera
    // operativo a cualquier hora, que es como funcionaban todos los recursos antes de esta
    // HU; así los recursos existentes no quedan sin disponibilidad al desplegar. En cuanto el
    // proveedor registra al menos una franja, el rango tiene que caber completo en alguna.
    // Los bloqueos aplican siempre, tenga o no horarios el recurso.
    public boolean esOperativo(LocalDateTime desde, LocalDateTime hasta) {
        if (!horarios.isEmpty() && horarios.stream().noneMatch(h -> h.contiene(desde, hasta))) {
            return false;
        }
        return bloqueos.stream().noneMatch(b -> b.seSolapaCon(desde, hasta));
    }

    // Unidades ocupadas por préstamos que se cruzan con el rango.
    public int comprometido(LocalDateTime desde, LocalDateTime hasta) {
        int comprometido = 0;
        for (RecursoPrestado prestamo : prestamosActivos) {
            if (prestamo.seSolapaCon(desde, hasta)) {
                comprometido += prestamo.getCantidad();
            }
        }
        return comprometido;
    }

    // Unidades libres en el rango; 0 si el recurso no se puede usar a esa hora.
    public int disponible(LocalDateTime desde, LocalDateTime hasta) {
        return esOperativo(desde, hasta) ? recurso.unidadesDisponibles(comprometido(desde, hasta)) : 0;
    }

    // ¿Se pueden apartar esas unidades en ese rango? Además de lo anterior, el recurso tiene
    // que estar activo.
    public boolean puedeAtender(int cantidad, LocalDateTime desde, LocalDateTime hasta) {
        return recurso.estaActivo() && disponible(desde, hasta) >= cantidad;
    }

    public Recurso getRecurso() { return recurso; }
}
