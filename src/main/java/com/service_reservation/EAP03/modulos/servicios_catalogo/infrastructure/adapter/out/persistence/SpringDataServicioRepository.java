package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataServicioRepository extends JpaRepository<ServicioJpaEntity, Integer>, JpaSpecificationExecutor<ServicioJpaEntity> {

    List<ServicioJpaEntity> findByIdProveedor(Integer idProveedor);
}
