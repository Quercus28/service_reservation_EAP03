package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;

import java.time.LocalDateTime;

/**
 * Solicitud de aprobación de una cuenta de proveedor.
 * Mientras no sea APROBADA, el ROLE_PROVEEDOR del usuario permanece sin permisos operativos.
 */
public class SolicitudAprobacionProveedor {

    public static final int MOTIVO_MAX = 500;

    private final Long id;
    private final Long idUsuario;
    private EstadoSolicitud estado;
    private final LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    private Long idAdminResolutor;
    private String motivoRechazo;

    public SolicitudAprobacionProveedor(Long id, Long idUsuario, EstadoSolicitud estado,
                                        LocalDateTime fechaSolicitud, LocalDateTime fechaResolucion,
                                        Long idAdminResolutor, String motivoRechazo) {
        if (idUsuario == null) {
            throw new DatosAdminInvalidosException("El usuario de la solicitud es obligatorio");
        }
        this.id = id;
        this.idUsuario = idUsuario;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaResolucion = fechaResolucion;
        this.idAdminResolutor = idAdminResolutor;
        this.motivoRechazo = motivoRechazo;
    }

    public static SolicitudAprobacionProveedor nueva(Long idUsuario, LocalDateTime ahora) {
        return new SolicitudAprobacionProveedor(null, idUsuario, EstadoSolicitud.PENDIENTE,
                ahora, null, null, null);
    }

    public void aprobar(Long idAdmin, LocalDateTime ahora) {
        validarPendiente();
        this.estado = EstadoSolicitud.APROBADA;
        this.idAdminResolutor = idAdmin;
        this.fechaResolucion = ahora;
    }

    public void rechazar(Long idAdmin, String motivo, LocalDateTime ahora) {
        validarPendiente();
        this.motivoRechazo = validarMotivo(motivo);
        this.estado = EstadoSolicitud.RECHAZADA;
        this.idAdminResolutor = idAdmin;
        this.fechaResolucion = ahora;
    }

    public boolean estaPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    private void validarPendiente() {
        if (!estaPendiente()) {
            throw new OperacionAdminInvalidaException(
                    "La solicitud " + id + " ya fue resuelta con estado " + estado);
        }
    }

    static String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new DatosAdminInvalidosException("El motivo es obligatorio");
        }
        String limpio = motivo.trim();
        if (limpio.length() > MOTIVO_MAX) {
            throw new DatosAdminInvalidosException("El motivo no puede superar " + MOTIVO_MAX + " caracteres");
        }
        return limpio;
    }

    public Long getId() { return id; }
    public Long getIdUsuario() { return idUsuario; }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public Long getIdAdminResolutor() { return idAdminResolutor; }
    public String getMotivoRechazo() { return motivoRechazo; }
}
