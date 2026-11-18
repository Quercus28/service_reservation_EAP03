package com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ConsultaOcupacionComando;
import com.service_reservation.EAP03.modulos.reportes.domain.model.ReporteOcupacion;
import com.service_reservation.EAP03.modulos.reportes.domain.ports.in.ObtenerReporteOcupacionUseCase;
import com.service_reservation.EAP03.modulos.reportes.infrastructure.inbound.web.dto.ReporteOcupacionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el módulo de reportes.
 * Expone la consulta de ocupación de servicios para proveedores autenticados.
 */
@RestController
@Tag(name = "Reportes", description = "Generación de reportes operativos y analíticos para proveedores.")
@PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
public class ReportesController {

    private static final Logger log = LoggerFactory.getLogger(ReportesController.class);

    private static final List<String> PARAMETROS_PROHIBIDOS = List.of(
            "providerId", "provider_id", "idProveedor", "id_proveedor", "proveedorId", "proveedor_id"
    );

    private final ObtenerReporteOcupacionUseCase obtenerReporteOcupacionUseCase;
    private final ConsultarProveedorUseCase consultarProveedorUseCase;

    public ReportesController(ObtenerReporteOcupacionUseCase obtenerReporteOcupacionUseCase,
                              ConsultarProveedorUseCase consultarProveedorUseCase) {
        this.obtenerReporteOcupacionUseCase = obtenerReporteOcupacionUseCase;
        this.consultarProveedorUseCase = consultarProveedorUseCase;
    }

    /**
     * Consulta el reporte de ocupación de servicios en un rango de fechas.
     */
    @GetMapping("/api/reportes/ocupacion")
    @Operation(summary = "Reporte de Ocupación de Servicios",
            description = "Calcula la ocupación real vs capacidad disponible para el proveedor autenticado en el período dado.")
    public ResponseEntity<ReporteOcupacionResponseDTO> consultarOcupacion(
            @Parameter(description = "Fecha de inicio (YYYY-MM-DD)", required = true)
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (YYYY-MM-DD)", required = true)
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication,
            HttpServletRequest request) {

        validarPeticionSegura(request, authentication);

        Integer proveedorId = extraerProveedorId(authentication);

        ConsultaOcupacionComando comando = new ConsultaOcupacionComando(proveedorId, startDate, endDate);
        ReporteOcupacion reporte = obtenerReporteOcupacionUseCase.ejecutar(comando);

        return ResponseEntity.ok(ReporteOcupacionResponseDTO.from(reporte));
    }

    /**
     * Valida que no se intenten inyectar identificadores de proveedor en la petición
     * para evitar manipulación o consultas cruzadas.
     * Para prevenir vulnerabilidades de Log Injection, no se registra el valor
     * arbitrario enviado por el usuario, solo el nombre del parámetro prohibido.
     */
    private void validarPeticionSegura(HttpServletRequest request, Authentication authentication) {
        for (String param : PARAMETROS_PROHIBIDOS) {
            if (request.getParameter(param) != null) {
                Long idUsuario = obtenerIdUsuario(authentication);
                log.warn("ALERTA DE SEGURIDAD: Usuario idUsuario={} intentó enviar parámetro prohibido '{}' en consulta de reporte",
                        idUsuario, param);
                throw new AccessDeniedException("No está permitido especificar el identificador del proveedor en la petición");
            }
        }
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

    private Long obtenerIdUsuario(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
            return principal.getIdUsuario();
        }
        return null;
    }
}
