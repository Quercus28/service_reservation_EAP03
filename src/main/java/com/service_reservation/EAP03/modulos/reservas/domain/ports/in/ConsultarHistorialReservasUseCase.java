package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

import com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;

// Historial del cliente autenticado: todas sus reservas, en cualquier estado.
public interface ConsultarHistorialReservasUseCase {
    PaginaHistorial ejecutar(ConsultarHistorialComando comando);
}