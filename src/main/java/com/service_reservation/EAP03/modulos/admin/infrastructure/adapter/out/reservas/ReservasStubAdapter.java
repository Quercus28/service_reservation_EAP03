package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.reservas;

import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import org.springframework.stereotype.Component;

@Component
public class ReservasStubAdapter implements ReservasPort {

    @Override
    public int contarReservasActivasDeCliente(Long idUsuario) {
        // TODO: Integration with Reservas module needed. Stub returning 0.
        return 0;
    }

    @Override
    public int contarReservasActivasDeProveedor(Long idUsuario) {
        // TODO: Integration with Reservas module needed. Stub returning 0.
        return 0;
    }

    @Override
    public int cancelarReservasActivasDeCliente(Long idUsuario, String motivo) {
        // TODO: Integration with Reservas module needed. Stub returning 0.
        return 0;
    }

    @Override
    public int cancelarReservasActivasDeProveedor(Long idUsuario, String motivo) {
        // TODO: Integration with Reservas module needed. Stub returning 0.
        return 0;
    }
}
