package com.service_reservation.EAP03.modulos.admin.domain.ports.out;

/**
 * Puerto hacia el módulo de reservas (aún no implementado).
 *
 * Contrato que el módulo de reservas deberá cumplir mediante un adapter:
 * - "Reserva activa" = reserva no cancelada ni finalizada.
 * - Los métodos reciben el id de USUARIO; el adapter es responsable de resolver
 *   el id de CLIENTE / PROVEEDOR correspondiente.
 * - Cancelar = cambio de estado a CANCELADA (soft delete) registrando el motivo recibido.
 * - Las cancelaciones deben ejecutarse dentro de la transacción del llamador.
 */
public interface ReservasPort {

    int contarReservasActivasDeCliente(Long idUsuario);

    int contarReservasActivasDeProveedor(Long idUsuario);

    /** @return número de reservas canceladas */
    int cancelarReservasActivasDeCliente(Long idUsuario, String motivo);

    /** @return número de reservas canceladas */
    int cancelarReservasActivasDeProveedor(Long idUsuario, String motivo);
}
