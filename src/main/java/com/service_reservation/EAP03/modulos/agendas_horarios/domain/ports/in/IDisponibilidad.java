package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;

import java.util.List;

public interface IDisponibilidad {
    List<Horario> consultarHorariosDisponibles(Long proveedorId);
}
