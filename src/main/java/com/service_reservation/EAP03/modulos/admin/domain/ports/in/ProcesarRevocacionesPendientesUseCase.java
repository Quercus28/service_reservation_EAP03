package com.service_reservation.EAP03.modulos.admin.domain.ports.in;

public interface ProcesarRevocacionesPendientesUseCase {

    /**
     * Revisa las revocaciones en plazo de gracia: completa las que ya no tienen reservas activas
     * y fuerza la cancelación de reservas de las que vencieron.
     *
     * @return número de revocaciones completadas en esta ejecución
     */
    int procesar();
}
