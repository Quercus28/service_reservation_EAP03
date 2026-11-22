package com.service_reservation.EAP03.modulos.admin.domain.model;

public enum AccionAdmin {
    SOLICITUD_PROVEEDOR_CREADA,
    SOLICITUD_PROVEEDOR_REENVIADA,
    SOLICITUD_PROVEEDOR_APROBADA,
    SOLICITUD_PROVEEDOR_RECHAZADA,
    PERFIL_PROVEEDOR_ACTUALIZADO,
    REVOCACION_INICIADA,
    REVOCACION_COMPLETADA,
    REVOCACION_FORZADA,
    PERMISOS_RESTAURADOS,
    ACCESO_DENEGADO,
    // Las usan otros módulos con RegistrarAuditoriaUseCase. HISTORIAL_AJENO_DENEGADO tiene el
    // mismo nombre que el evento que reservas ya escribe en el log, para no tener dos nombres.
    HISTORIAL_AJENO_DENEGADO,
    CAMBIO_ESTADO_RESERVA_DENEGADO,
    // Un proveedor intentó pedir el reporte de ocupación nombrando a un proveedor en la petición.
    REPORTE_AJENO_DENEGADO
}
