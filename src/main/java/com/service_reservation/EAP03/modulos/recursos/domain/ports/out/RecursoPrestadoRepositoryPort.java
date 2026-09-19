package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import java.time.LocalDateTime;
import java.util.List;

public interface RecursoPrestadoRepositoryPort {
    RecursoPrestado guardar(RecursoPrestado recursoPrestado);
    List<RecursoPrestado> buscarQueSeSolapan(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta);
}
