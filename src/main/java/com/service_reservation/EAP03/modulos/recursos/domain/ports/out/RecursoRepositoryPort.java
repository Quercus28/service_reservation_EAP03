package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import java.util.List;
import java.util.Optional;

public interface RecursoRepositoryPort {
    Recurso guardar(Recurso recurso);
    Optional<Recurso> buscarPorId(Integer id);
    List<Recurso> buscarPorProveedor(Integer idProveedor);
    boolean existePorId(Integer id);
    void eliminarPorId(Integer id);
}
