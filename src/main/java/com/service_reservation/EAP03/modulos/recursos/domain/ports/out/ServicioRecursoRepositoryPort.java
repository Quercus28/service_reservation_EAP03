package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;

import java.util.List;
import java.util.Optional;

public interface ServicioRecursoRepositoryPort {
    ServicioRecurso guardar(ServicioRecurso servicioRecurso);
    Optional<ServicioRecurso> buscarPorServicioYRecurso(Integer idServicio, Integer idRecurso);
    boolean existePorServicioYRecurso(Integer idServicio, Integer idRecurso);
    List<ServicioRecurso> buscarPorServicio(Integer idServicio);
    void eliminar(Integer id);
}
