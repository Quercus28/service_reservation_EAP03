package com.service_reservation.EAP03.modulos.identidad.domain.model;

public record UsuarioNuevoComando(
    String email, 
    String password, 
    String rol, 
    String nombreIdentificacion,
    String telefono,
    String documentoIdentidad
) {}