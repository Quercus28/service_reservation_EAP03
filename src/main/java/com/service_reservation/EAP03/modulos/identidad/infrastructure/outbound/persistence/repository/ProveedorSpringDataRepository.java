package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.ProveedorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


public interface ProveedorSpringDataRepository extends JpaRepository<ProveedorJpaEntity, Long> {

    @Query(value = "SELECT p.id FROM PROVEEDOR p JOIN USUARIO u ON u.id = p.id_usuario WHERE u.email = :email", nativeQuery = true)
    Optional<Long> findIdByUsuarioEmail(@Param("email") String email);

    Optional<ProveedorJpaEntity> findByIdUsuario(Long idUsuario);
}