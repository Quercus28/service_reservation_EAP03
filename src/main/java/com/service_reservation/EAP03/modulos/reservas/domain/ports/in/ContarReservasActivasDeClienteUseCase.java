package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

// Para otros módulos (por ejemplo admin): cuántas reservas activas (RESERVADO o EN_PROCESO)
// tiene un cliente. Recibe el id del USUARIO; si ese usuario no es cliente, devuelve 0.
public interface ContarReservasActivasDeClienteUseCase {
    int ejecutar(Long idUsuario);
}
