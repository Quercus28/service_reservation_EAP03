package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

// Reglas de @PreAuthorize compartidas por los controladores de recursos.
final class ReglasAccesoRecurso {

    // El proveedor además tiene que tener sus permisos activos: si el administrador se los
    // quitó (o todavía no aprobó su cuenta), ya no puede tocar el recurso.
    static final String PROPIETARIO_O_ADMIN =
            "hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')"
                    + " and @recursoSecurity.esPropietario(authentication, #id))";

    private ReglasAccesoRecurso() {
    }
}
