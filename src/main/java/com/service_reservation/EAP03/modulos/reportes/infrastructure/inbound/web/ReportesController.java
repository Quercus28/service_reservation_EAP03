package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.in.ObtenerReporteOcupacionUseCase;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto.ReporteOcupacionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el módulo de reportes.
 * Expone la consulta de ocupación de servicios para proveedores autenticados.
 */
@RestController
@Tag(name = "Reportes", description = "Generación de reportes operativos y analíticos para proveedores.")
@PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
public class ReportesController {

    private static final Logger log = LoggerFactory.getLogger(ReportesController.class);

    private final ObtenerReporteOcupacionUseCase obtenerReporteOcupacionUseCase;
    private final ConsultarProveedorUseCase consultarProveedorUseCase;

    public ReportesController(ObtenerReporteOcupacionUseCase obtenerReporteOcupacionUseCase,
                              ConsultarProveedorUseCase consultarProveedorUseCase) {
        this.obtenerReporteOcupacionUseCase = obtenerReporteOcupacionUseCase;
        this.consultarProveedorUseCase = consultarProveedorUseCase;
    }

    /**
     * Consulta el reporte de ocupación de servicios en un rango de fechas.
     * Los parámetros se leen y validan con lista blanca en {@link ConsultaOcupacionParametros}
     * (no con @RequestParam), para que una fecha faltante, un parámetro desconocido o repetido
     * y un parámetro que nombre a un proveedor respondan con el formato estándar de error.
     */
    @GetMapping("/api/reportes/ocupacion")
    @Operation(summary = "Reporte de Ocupación de Servicios",
            description = "Calcula la ocupación real vs capacidad disponible para el proveedor autenticado en el período dado. "
                    + "Solo acepta startDate y endDate (YYYY-MM-DD, obligatorios, una vez cada uno) y un período de hasta 366 días. "
                    + "Si la petición nombra a un proveedor (proveedorId, idProveedor...) responde 403 y el intento queda en la auditoría.")
    @Parameter(name = "startDate", in = ParameterIn.QUERY, required = true,
            description = "Fecha de inicio (YYYY-MM-DD)", schema = @Schema(type = "string", format = "date"))
    @Parameter(name = "endDate", in = ParameterIn.QUERY, required = true,
            description = "Fecha de fin (YYYY-MM-DD)", schema = @Schema(type = "string", format = "date"))
    public ResponseEntity<ReporteOcupacionResponseDTO> consultarOcupacion(
            @Parameter(hidden = true) HttpServletRequest request,
            Authentication authentication) {

        ConsultaOcupacionParametros parametros = ConsultaOcupacionParametros.desde(request.getParameterMap());

        Integer proveedorId = extraerProveedorId(authentication);

        ConsultaOcupacionComando comando = new ConsultaOcupacionComando(
                proveedorId, parametros.startDate(), parametros.endDate());
        ReporteOcupacion reporte = obtenerReporteOcupacionUseCase.ejecutar(comando);

        return ResponseEntity.ok(ReporteOcupacionResponseDTO.from(reporte));
    }

    /**
     * Extrae el ID del proveedor a partir de la identidad autenticada (JWT).
     */
    private Integer extraerProveedorId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
            log.warn("Intento de acceso al reporte sin principal autenticado válido");
            throw new AccessDeniedException("Usuario no autenticado o principal inválido");
        }

        return consultarProveedorUseCase.buscarIdProveedorPorUsuario(principal.getIdUsuario())
                .orElseThrow(() -> {
                    log.warn("AUDITORÍA DE SEGURIDAD: El usuario autenticado idUsuario={} con rol PROVEEDOR no cuenta con un registro en la tabla de proveedores",
                            principal.getIdUsuario());
                    return new AccessDeniedException("El usuario autenticado no tiene un perfil de proveedor asociado");
                });
    }
}
