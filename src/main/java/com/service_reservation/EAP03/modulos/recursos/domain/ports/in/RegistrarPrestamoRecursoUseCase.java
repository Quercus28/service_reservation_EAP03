package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;

public interface RegistrarPrestamoRecursoUseCase {
    RecursoPrestado ejecutar(RegistrarPrestamoComando comando);
}
