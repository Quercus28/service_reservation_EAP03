package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Revocación de permisos operativos sobre un rol de un usuario.
 *
 * Reglas de negocio:
 * - CLIENTE: la revocación se completa de inmediato y sus reservas activas se cancelan automáticamente.
 * - PROVEEDOR sin reservas activas: se completa de inmediato.
 * - PROVEEDOR con reservas activas: queda PENDIENTE_CANCELACION con un plazo de gracia de
 *   {@link #PLAZO_GRACIA} para que el proveedor las cancele. Vencido el plazo, el sistema
 *   las cancela forzosamente y completa la revocación.
 */
public class RevocacionPermiso {

    public static final Duration PLAZO_GRACIA = Duration.ofDays(5);
    public static final String MOTIVO_CANCELACION_RESERVAS = "REVOCACION_ADMIN";

    private final Long id;
    private final Long idUsuario;
    private final RolOperativo rol;
    private EstadoRevocacion estado;
    private final String motivo;
    private final Long idAdmin;
    private final LocalDateTime fechaSolicitud;
    private final LocalDateTime fechaLimite;
    private LocalDateTime fechaCierre;
    private int reservasCanceladas;

    public RevocacionPermiso(Long id, Long idUsuario, RolOperativo rol, EstadoRevocacion estado,
                             String motivo, Long idAdmin, LocalDateTime fechaSolicitud,
                             LocalDateTime fechaLimite, LocalDateTime fechaCierre, int reservasCanceladas) {
        if (idUsuario == null || rol == null || idAdmin == null) {
            throw new DatosAdminInvalidosException("Usuario, rol y administrador son obligatorios en una revocación");
        }
        this.id = id;
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.estado = estado;
        this.motivo = motivo;
        this.idAdmin = idAdmin;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaLimite = fechaLimite;
        this.fechaCierre = fechaCierre;
        this.reservasCanceladas = reservasCanceladas;
    }

    /** Revocación que se cierra en el mismo instante en que se solicita. */
    public static RevocacionPermiso inmediata(Long idUsuario, RolOperativo rol, String motivo, Long idAdmin,
                                              LocalDateTime ahora, int reservasCanceladas) {
        return new RevocacionPermiso(null, idUsuario, rol, EstadoRevocacion.COMPLETADA,
                SolicitudAprobacionProveedor.validarMotivo(motivo), idAdmin,
                ahora, null, ahora, reservasCanceladas);
    }

    /** Revocación de proveedor con reservas activas: abre el plazo de gracia. */
    public static RevocacionPermiso conPlazoDeGracia(Long idUsuario, String motivo, Long idAdmin, LocalDateTime ahora) {
        return new RevocacionPermiso(null, idUsuario, RolOperativo.PROVEEDOR, EstadoRevocacion.PENDIENTE_CANCELACION,
                SolicitudAprobacionProveedor.validarMotivo(motivo), idAdmin,
                ahora, ahora.plus(PLAZO_GRACIA), null, 0);
    }

    public boolean estaPendiente() {
        return estado == EstadoRevocacion.PENDIENTE_CANCELACION;
    }

    public boolean plazoVencido(LocalDateTime ahora) {
        return fechaLimite != null && !ahora.isBefore(fechaLimite);
    }

    public void completar(LocalDateTime ahora, int reservasCanceladasForzosamente) {
        validarPendiente();
        this.estado = EstadoRevocacion.COMPLETADA;
        this.fechaCierre = ahora;
        this.reservasCanceladas = reservasCanceladasForzosamente;
    }

    public void anular(LocalDateTime ahora) {
        validarPendiente();
        this.estado = EstadoRevocacion.ANULADA;
        this.fechaCierre = ahora;
    }

    private void validarPendiente() {
        if (!estaPendiente()) {
            throw new OperacionAdminInvalidaException(
                    "La revocación " + id + " ya está cerrada con estado " + estado);
        }
    }

    public Long getId() { return id; }
    public Long getIdUsuario() { return idUsuario; }
    public RolOperativo getRol() { return rol; }
    public EstadoRevocacion getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public Long getIdAdmin() { return idAdmin; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public int getReservasCanceladas() { return reservasCanceladas; }
}
