package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BloqueoRecursoRepositoryPort {
    BloqueoRecurso guardar(BloqueoRecurso bloqueo);
    Optional<BloqueoRecurso> buscarPorId(Integer id);
    List<BloqueoRecurso> buscarQueSeSolapan(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta);
    List<BloqueoRecurso> buscarQueSeSolapanDeRecursos(Collection<Integer> idsRecurso, LocalDateTime desde, LocalDateTime hasta);
    ResultadoPaginado<BloqueoRecurso> buscarPorRecurso(Integer idRecurso, int pagina, int tamano);
    void eliminar(Integer id);
}
