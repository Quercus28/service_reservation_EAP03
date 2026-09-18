package com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out;

public interface ProveedorIdentidadPort {
    Long obtenerProveedorIdPorEmail(String email);
}
