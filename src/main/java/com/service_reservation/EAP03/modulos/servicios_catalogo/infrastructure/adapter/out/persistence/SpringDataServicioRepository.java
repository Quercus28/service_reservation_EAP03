package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataServicioRepository extends JpaRepository<ServicioJpaEntity, Integer> {

    List<ServicioJpaEntity> findByIdProveedor(Integer idProveedor);
}
