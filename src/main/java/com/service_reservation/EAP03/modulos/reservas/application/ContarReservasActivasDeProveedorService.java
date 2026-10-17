package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ContarReservasActivasDeProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cuenta las reservas activas (RESERVADO o EN_PROCESO) de un proveedor. Lo usa el módulo admin.
// El usuario se pasa a proveedor con el caso de uso de identidad, no leyendo sus tablas.
@Service
public class ContarReservasActivasDeProveedorService implements ContarReservasActivasDeProveedorUseCase {

    private final ConsultarProveedorUseCase consultarProveedorUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;

    public ContarReservasActivasDeProveedorService(ConsultarProveedorUseCase consultarProveedorUseCase,
                                                   ReservaRepositoryPort reservaRepositoryPort) {
        this.consultarProveedorUseCase = consultarProveedorUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public int ejecutar(Long idUsuario) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarProveedorUseCase.buscarIdProveedorPorUsuario(idUsuario)
                .map(reservaRepositoryPort::contarActivasDeProveedor)
                .orElse(0);
    }
}
