package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Turno;

import java.util.List;

// Para la consulta de disponibilidad: de los turnos que recibe, devuelve solo aquellos en los
// que todos los recursos obligatorios del servicio se pueden usar. Si el servicio no tiene
// recursos obligatorios, devuelve los turnos tal cual.
public interface FiltrarTurnosConRecursosDisponiblesUseCase {
    List<Turno> ejecutar(Integer idServicio, List<Turno> turnos);
}
