package com.service_reservation.EAP03.modulos.admin.domain.model;

public record ActualizarProveedorPerfilComando(
        Long idAdmin,
        Long idUsuario,
        String razonSocial,
        String nitRut,
        String telefono
) {}
