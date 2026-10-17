package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ProveedorPerfilPort;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProveedorPerfilPersistenceAdapter implements ProveedorPerfilPort {

    private final JdbcTemplate jdbcTemplate;

    public ProveedorPerfilPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProveedorPerfilDTO> buscarPorIdUsuario(Long idUsuario) {
        String sql = """
                SELECT id_usuario, razon_social, nit_rut, telefono
                FROM PROVEEDOR
                WHERE id_usuario = ?
                """;
        try {
            ProveedorPerfilDTO perfil = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new ProveedorPerfilDTO(
                    rs.getLong("id_usuario"),
                    rs.getString("razon_social"),
                    rs.getString("nit_rut"),
                    rs.getString("telefono")
            ), idUsuario);
            return Optional.ofNullable(perfil);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void actualizarPerfil(Long idUsuario, String razonSocial, String nitRut, String telefono) {
        String sql = """
                UPDATE PROVEEDOR
                SET razon_social = ?, nit_rut = ?, telefono = ?
                WHERE id_usuario = ?
                """;
        jdbcTemplate.update(sql, razonSocial, nitRut, telefono, idUsuario);
    }
}
