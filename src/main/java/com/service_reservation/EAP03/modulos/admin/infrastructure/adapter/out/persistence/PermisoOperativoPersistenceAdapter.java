package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.PermisoOperativoPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PermisoOperativoPersistenceAdapter implements PermisoOperativoPort {

    private final JdbcTemplate jdbcTemplate;

    public PermisoOperativoPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Boolean> estadoPermiso(Long idUsuario, RolOperativo rol) {
        String sql = """
                SELECT ur.activo 
                FROM USUARIO_ROL ur
                JOIN ROL r ON r.id = ur.id_rol
                WHERE ur.id_usuario = ? AND r.nombre = ?
                """;
        List<Boolean> resultados = jdbcTemplate.queryForList(sql, Boolean.class, idUsuario, rol.authority());
        if (resultados.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultados.get(0));
    }

    @Override
    public void actualizarEstado(Long idUsuario, RolOperativo rol, boolean activo) {
        String sql = """
                UPDATE USUARIO_ROL ur
                SET activo = ?
                WHERE ur.id_usuario = ? 
                  AND ur.id_rol = (SELECT r.id FROM ROL r WHERE r.nombre = ?)
                """;
        jdbcTemplate.update(sql, activo, idUsuario, rol.authority());
    }

    @Override
    public Map<RolOperativo, Boolean> listarPermisos(Long idUsuario) {
        String sql = """
                SELECT r.nombre, ur.activo
                FROM USUARIO_ROL ur
                JOIN ROL r ON r.id = ur.id_rol
                WHERE ur.id_usuario = ? AND r.nombre IN ('ROLE_CLIENTE', 'ROLE_PROVEEDOR')
                """;
        
        Map<RolOperativo, Boolean> permisos = new EnumMap<>(RolOperativo.class);
        jdbcTemplate.query(sql, rs -> {
            boolean activo = rs.getBoolean("activo");
            permisos.put(RolOperativo.desde(rs.getString("nombre")), activo);
        }, idUsuario);
        
        return permisos;
    }
}
