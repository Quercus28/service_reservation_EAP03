package com.service_reservation.EAP03.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un DTO de entrada para que un campo que no existe en él haga fallar la petición (HTTP 400)
 * en lugar de descartarse en silencio. Lo aplica {@link UnknownFieldsConfig}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RejectUnknownFields {
}
