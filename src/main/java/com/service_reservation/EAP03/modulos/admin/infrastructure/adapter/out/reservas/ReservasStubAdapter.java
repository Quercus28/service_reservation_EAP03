package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.reservas;

import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservasActivasDeProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeProveedorUseCase;
import org.springframework.stereotype.Component;

@Component
public class ReservasStubAdapter implements ReservasPort {

    private final ContarReservasActivasDeClienteUseCase contarReservasActivasDeClienteUseCase;
    private final ContarReservasActivasDeProveedorUseCase contarReservasActivasDeProveedorUseCase;
    private final CancelarReservasActivasDeClienteUseCase cancelarReservasActivasDeClienteUseCase;
    private final CancelarReservasActivasDeProveedorUseCase cancelarReservasActivasDeProveedorUseCase;

    public ReservasStubAdapter(
            ContarReservasActivasDeClienteUseCase contarReservasActivasDeClienteUseCase,
            ContarReservasActivasDeProveedorUseCase contarReservasActivasDeProveedorUseCase,
            CancelarReservasActivasDeClienteUseCase cancelarReservasActivasDeClienteUseCase,
            CancelarReservasActivasDeProveedorUseCase cancelarReservasActivasDeProveedorUseCase) {
        this.contarReservasActivasDeClienteUseCase = contarReservasActivasDeClienteUseCase;
        this.contarReservasActivasDeProveedorUseCase = contarReservasActivasDeProveedorUseCase;
        this.cancelarReservasActivasDeClienteUseCase = cancelarReservasActivasDeClienteUseCase;
        this.cancelarReservasActivasDeProveedorUseCase = cancelarReservasActivasDeProveedorUseCase;
    }

    @Override
    public int contarReservasActivasDeCliente(Long idUsuario) {
        return contarReservasActivasDeClienteUseCase.ejecutar(idUsuario);
    }

    @Override
    public int contarReservasActivasDeProveedor(Long idUsuario) {
        return contarReservasActivasDeProveedorUseCase.ejecutar(idUsuario);
    }

    @Override
    public int cancelarReservasActivasDeCliente(Long idUsuario, String motivo) {
        return cancelarReservasActivasDeClienteUseCase.ejecutar(idUsuario, motivo);
    }

    @Override
    public int cancelarReservasActivasDeProveedor(Long idUsuario, String motivo) {
        return cancelarReservasActivasDeProveedorUseCase.ejecutar(idUsuario, motivo);
    }
}

