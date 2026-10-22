package com.service_reservation.EAP03.modulos.recursos.domain.ports.in;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// Para el módulo de reservas: aparta los recursos obligatorios del servicio más los opcionales
// que eligió el cliente. Si alguno no se puede apartar, lanza el error y no aparta ninguno.
public interface ApartarRecursosDeReservaUseCase {
    List<RecursoPrestado> ejecutar(Integer idReserva, Integer idServicio, LocalDateTime inicio,
                                   LocalDateTime fin, Collection<Integer> idsOpcionalesElegidos);
}
