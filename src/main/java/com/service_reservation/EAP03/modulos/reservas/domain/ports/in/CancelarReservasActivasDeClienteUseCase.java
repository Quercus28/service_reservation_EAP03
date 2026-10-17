package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

// Para el módulo admin: cancela todas las reservas activas (RESERVADO o EN_PROCESO) de un
// cliente, sin la regla de las horas de anticipación. Recibe el id del USUARIO y devuelve
// cuántas reservas se cancelaron (0 si el usuario no es cliente o no tenía ninguna).
public interface CancelarReservasActivasDeClienteUseCase {
    int ejecutar(Long idUsuario, String motivo);
}
