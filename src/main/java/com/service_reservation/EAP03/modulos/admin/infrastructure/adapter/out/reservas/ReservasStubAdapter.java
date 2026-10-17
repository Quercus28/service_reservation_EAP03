package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.reservas;

import com.service_reservation.EAP03.modulos.admin.domain.ports.out.ReservasPort;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class ReservasStubAdapter implements ReservasPort {

    private static final int MOTIVO_MAX_LENGTH = 255;

    private final JdbcTemplate jdbcTemplate;
    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ConsultarProveedorUseCase consultarProveedorUseCase;
    private final Clock clock;

    public ReservasStubAdapter(JdbcTemplate jdbcTemplate,
                               ConsultarClienteUseCase consultarClienteUseCase,
                               ConsultarProveedorUseCase consultarProveedorUseCase,
                               Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.consultarProveedorUseCase = consultarProveedorUseCase;
        this.clock = clock;
    }

    @Override
    public int contarReservasActivasDeCliente(Long idUsuario) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarClienteUseCase.buscarIdClientePorUsuario(idUsuario)
                .map(this::contarActivasPorIdCliente)
                .orElse(0);
    }

    @Override
    public int contarReservasActivasDeProveedor(Long idUsuario) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarProveedorUseCase.buscarIdProveedorPorUsuario(idUsuario)
                .map(this::contarActivasPorIdProveedor)
                .orElse(0);
    }

    @Override
    public int cancelarReservasActivasDeCliente(Long idUsuario, String motivo) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarClienteUseCase.buscarIdClientePorUsuario(idUsuario)
                .map(idCliente -> cancelarActivasPorIdCliente(idCliente, motivo))
                .orElse(0);
    }

    @Override
    public int cancelarReservasActivasDeProveedor(Long idUsuario, String motivo) {
        if (idUsuario == null) {
            return 0;
        }
        return consultarProveedorUseCase.buscarIdProveedorPorUsuario(idUsuario)
                .map(idProveedor -> cancelarActivasPorIdProveedor(idProveedor, motivo))
                .orElse(0);
    }

    private int contarActivasPorIdCliente(Long idCliente) {
        String sql = """
                SELECT COUNT(*)
                FROM servicio_prestado
                WHERE id_cliente = ? AND estado IN ('RESERVADO', 'EN_PROCESO')
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idCliente);
        return count != null ? count : 0;
    }

    private int contarActivasPorIdProveedor(Integer idProveedor) {
        String sql = """
                SELECT COUNT(*)
                FROM servicio_prestado
                WHERE id_proveedor = ? AND estado IN ('RESERVADO', 'EN_PROCESO')
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idProveedor);
        return count != null ? count : 0;
    }

    private int cancelarActivasPorIdCliente(Long idCliente, String motivo) {
        String sql = """
                UPDATE servicio_prestado
                SET estado = 'CANCELADO',
                    cancelada_en = ?,
                    motivo_cancelacion = ?
                WHERE id_cliente = ? AND estado IN ('RESERVADO', 'EN_PROCESO')
                """;
        LocalDateTime ahora = LocalDateTime.now(clock);
        String motivoSanitizado = sanitizarMotivo(motivo);
        return jdbcTemplate.update(sql, ahora, motivoSanitizado, idCliente);
    }

    private int cancelarActivasPorIdProveedor(Integer idProveedor, String motivo) {
        String sql = """
                UPDATE servicio_prestado
                SET estado = 'CANCELADO',
                    cancelada_en = ?,
                    motivo_cancelacion = ?
                WHERE id_proveedor = ? AND estado IN ('RESERVADO', 'EN_PROCESO')
                """;
        LocalDateTime ahora = LocalDateTime.now(clock);
        String motivoSanitizado = sanitizarMotivo(motivo);
        return jdbcTemplate.update(sql, ahora, motivoSanitizado, idProveedor);
    }

    private String sanitizarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        String limpio = motivo.trim();
        return limpio.length() > MOTIVO_MAX_LENGTH ? limpio.substring(0, MOTIVO_MAX_LENGTH) : limpio;
    }
}
