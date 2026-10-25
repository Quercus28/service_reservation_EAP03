package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.*;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.*;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.config.RecursoSecurity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recursos")
public class RecursoController {

    private final CrearRecursoUseCase crearRecursoUseCase;
    private final ActualizarRecursoUseCase actualizarRecursoUseCase;
    private final ConsultarRecursosUseCase consultarRecursosUseCase;
    private final ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase;
    private final DesactivarRecursoUseCase desactivarRecursoUseCase;
    private final RecursoSecurity recursoSecurity;

    public RecursoController(CrearRecursoUseCase crearRecursoUseCase,
                             ActualizarRecursoUseCase actualizarRecursoUseCase,
                             ConsultarRecursosUseCase consultarRecursosUseCase,
                             ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase,
                             DesactivarRecursoUseCase desactivarRecursoUseCase,
                             RecursoSecurity recursoSecurity) {
        this.crearRecursoUseCase = crearRecursoUseCase;
        this.actualizarRecursoUseCase = actualizarRecursoUseCase;
        this.consultarRecursosUseCase = consultarRecursosUseCase;
        this.obtenerRecursoPorIdUseCase = obtenerRecursoPorIdUseCase;
        this.desactivarRecursoUseCase = desactivarRecursoUseCase;
        this.recursoSecurity = recursoSecurity;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
    public RecursoResponseDTO crear(@Valid @RequestBody CrearRecursoRequestDTO request,
                                    Authentication authentication) {
        Integer idProveedor = recursoSecurity.idProveedorDe(authentication)
                .orElseThrow(() -> new DatosInvalidosException("El usuario autenticado no tiene un perfil de proveedor asociado"));

        Recurso recurso = crearRecursoUseCase.ejecutar(new CrearRecursoComando(
                idProveedor,
                request.nombre(),
                request.precioUnitario(),
                request.stock()
        ));
        return RecursoResponseDTO.desde(recurso);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
    public PaginaResponseDTO<RecursoResponseDTO> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication authentication) {

        Integer idProveedor = recursoSecurity.idProveedorDe(authentication)
                .orElseThrow(() -> new DatosInvalidosException("El usuario autenticado no tiene un perfil de proveedor asociado"));

        ResultadoPaginado<Recurso> resultado = consultarRecursosUseCase.ejecutar(idProveedor, pagina, tamano);

        List<RecursoResponseDTO> contenido = resultado.getContenido().stream()
                .map(RecursoResponseDTO::desde)
                .toList();

        return new PaginaResponseDTO<>(
                contenido,
                resultado.getPagina(),
                resultado.getTamano(),
                resultado.getTotalElementos(),
                resultado.getTotalPaginas()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR') and @recursoSecurity.esPropietario(authentication, #id))")
    public RecursoResponseDTO obtener(@PathVariable Integer id) {
        return RecursoResponseDTO.desde(obtenerRecursoPorIdUseCase.ejecutar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR') and @recursoSecurity.esPropietario(authentication, #id))")
    public RecursoResponseDTO actualizar(@PathVariable Integer id,
                                         @Valid @RequestBody ActualizarRecursoRequestDTO request) {
        Recurso recurso = actualizarRecursoUseCase.ejecutar(new ActualizarRecursoComando(
                id,
                request.nombre(),
                request.precioUnitario(),
                request.stock()
        ));
        return RecursoResponseDTO.desde(recurso);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR') and @recursoSecurity.esPropietario(authentication, #id))")
    public void desactivar(@PathVariable Integer id) {
        desactivarRecursoUseCase.ejecutar(id);
    }
}
