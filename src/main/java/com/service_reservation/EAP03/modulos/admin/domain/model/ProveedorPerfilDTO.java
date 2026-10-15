package com.service_reservation.EAP03.modulos.admin.domain.model;

public record ProveedorPerfilDTO(
        Long idUsuario,
        String razonSocial,
        String nitRut,
        String telefono
) {}
