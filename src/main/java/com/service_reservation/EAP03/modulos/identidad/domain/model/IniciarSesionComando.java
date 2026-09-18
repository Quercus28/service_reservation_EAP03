package com.service_reservation.EAP03.modulos.identidad.domain.model;

public record IniciarSesionComando(
    String email,
    String password
) {}
