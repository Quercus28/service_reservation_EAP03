package com.service_reservation.EAP03.modulos.recursos.domain.ports.out;

import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import java.util.Optional;

public interface RecursoRepositoryPort {
    Recurso guardar(Recurso recurso);
    Optional<Recurso> buscarPorId(Integer id);
    Optional<Recurso> buscarPorIdConBloqueo(Integer id);
    ResultadoPaginado<Recurso> buscarActivosPorProveedor(Integer idProveedor, int pagina, int tamano);
    boolean existePorId(Integer id);
    boolean existeNombreActivo(Integer idProveedor, String nombre, Integer idExcluir);
}
