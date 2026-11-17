package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAuditoriaUseCase;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.VerificarPermisoOperativoUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsProveedorException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CambiarEstadoReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CambiarEstadoReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CambiarEstadoReservaService implements CambiarEstadoReservaUseCase {

    private final VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase;
    private final ConsultarProveedorUseCase consultarProveedorUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;
    private final RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    public CambiarEstadoReservaService(VerificarPermisoOperativoUseCase verificarPermisoOperativoUseCase,
                                       ConsultarProveedorUseCase consultarProveedorUseCase,
                                       ReservaRepositoryPort reservaRepositoryPort,
                                       RegistrarAuditoriaUseCase registrarAuditoriaUseCase) {
        this.verificarPermisoOperativoUseCase = verificarPermisoOperativoUseCase;
        this.consultarProveedorUseCase = consultarProveedorUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.registrarAuditoriaUseCase = registrarAuditoriaUseCase;
    }

    // Quién puede cambiar el estado se revisa aquí y no en @PreAuthorize: si lo rechazara
    // @PreAuthorize, el 403 lo respondería la configuración de seguridad general y el intento
    // no quedaría en la auditoría. Aquí se rechaza y se audita en el mismo lugar.
    // El registro de auditoría va en su propia transacción, así que queda guardado aunque esta
    // se deshaga al lanzar el error.
    @Override
    @Transactional
    public Reserva ejecutar(CambiarEstadoReservaComando comando) {
        if (comando.idReserva() == null || comando.nuevoEstado() == null) {
            throw new DatosReservaInvalidosException("El identificador de la reserva y el estado son obligatorios");
        }

        // 1. Tiene que ser proveedor con su permiso activo (un cliente no lo es).
        Optional<Integer> idProveedor = comando.idUsuario() == null
                || !verificarPermisoOperativoUseCase.tienePermisoOperativo(comando.idUsuario(), RolOperativo.PROVEEDOR)
                ? Optional.empty()
                : consultarProveedorUseCase.buscarIdProveedorPorUsuario(comando.idUsuario());
        if (idProveedor.isEmpty()) {
            auditarDenegado(comando, "motivo=no es proveedor con permiso activo");
            throw new UsuarioNoEsProveedorException();
        }

        // 2. La reserva tiene que existir.
        Reserva reserva = reservaRepositoryPort.buscarPorId(comando.idReserva())
                .orElseThrow(() -> new ReservaNoEncontradaException(comando.idReserva()));

        // 3. Solo el proveedor dueño de la reserva puede cambiarla.
        if (!reserva.getIdProveedor().equals(idProveedor.get())) {
            auditarDenegado(comando, "motivo=reserva de otro proveedor");
            throw ReservaAjenaException.deOtroProveedor(comando.idReserva());
        }

        // 4. La reserva revisa si el cambio está permitido desde su estado actual.
        reserva.cambiarEstado(comando.nuevoEstado());
        return reservaRepositoryPort.guardar(reserva);
    }

    // Solo números y textos fijos: nunca correo, nombre ni otro dato personal.
    private void auditarDenegado(CambiarEstadoReservaComando comando, String motivo) {
        registrarAuditoriaUseCase.ejecutar(new EventoAuditoria(
                comando.idUsuario(),
                null,
                AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO,
                ResultadoAuditoria.DENEGADO,
                comando.endpoint(),
                "idReserva=" + comando.idReserva() + " estadoPedido=" + comando.nuevoEstado() + " " + motivo
        ));
    }
}
