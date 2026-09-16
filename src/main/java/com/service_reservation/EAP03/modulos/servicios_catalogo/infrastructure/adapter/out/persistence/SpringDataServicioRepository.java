package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataServicioRepository extends JpaRepository<ServicioJpaEntity, Integer> {

    List<ServicioJpaEntity> findByIdProveedor(Integer idProveedor);

    @Query(
            value = """
                SELECT s FROM ServicioJpaEntity s
                WHERE (:idProveedor IS NULL OR s.idProveedor = :idProveedor)
                  AND (:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
            """,
            countQuery = """
                SELECT count(s) FROM ServicioJpaEntity s
                WHERE (:idProveedor IS NULL OR s.idProveedor = :idProveedor)
                  AND (:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
            """
    )
    Page<ServicioJpaEntity> buscarConFiltros(
            @Param("idProveedor") Integer idProveedor,
            @Param("nombre") String nombre,
            Pageable pageable
    );
}
