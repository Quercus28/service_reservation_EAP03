package com.service_reservation.EAP03.modulos.reservas.domain.ports.in;

// Para otros módulos (por ejemplo admin): cuántas reservas activas (RESERVADO o EN_PROCESO)
// tiene un proveedor. Recibe el id del USUARIO; si ese usuario no es proveedor, devuelve 0.
public interface ContarReservasActivasDeProveedorUseCase {
    int ejecutar(Long idUsuario);
}
