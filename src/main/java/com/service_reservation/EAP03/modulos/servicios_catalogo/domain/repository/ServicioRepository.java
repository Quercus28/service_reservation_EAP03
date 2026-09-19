package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository {

    Servicio guardar(Servicio servicio);

    Optional<Servicio> buscarPorId(Integer id);

    List<Servicio> buscarPorProveedor(Integer idProveedor);

    ResultadoPaginado<Servicio> buscarPaginado(Integer idProveedor, String nombre, int pagina, int tamano);

    boolean existePorId(Integer id);

    void eliminarPorId(Integer id);
}
