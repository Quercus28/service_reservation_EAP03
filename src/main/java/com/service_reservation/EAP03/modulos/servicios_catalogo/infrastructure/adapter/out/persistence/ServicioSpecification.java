package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ServicioSpecification {

    private ServicioSpecification() {
    }

    public static Specification<ServicioJpaEntity> conFiltros(Integer idProveedor, String nombre) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (idProveedor != null) {
                predicates.add(criteriaBuilder.equal(root.get("idProveedor"), idProveedor));
            }

            if (nombre != null && !nombre.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        "%" + nombre.trim().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
