package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ActualizarServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ConsultarServiciosUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.CrearServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.EliminarServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final CrearServicioUseCase crearServicioUseCase;
    private final ActualizarServicioUseCase actualizarServicioUseCase;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final ConsultarServiciosUseCase consultarServiciosUseCase;
    private final EliminarServicioUseCase eliminarServicioUseCase;

    @Autowired
    public ServicioController(
            CrearServicioUseCase crearServicioUseCase,
            ActualizarServicioUseCase actualizarServicioUseCase,
            ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
            ConsultarServiciosUseCase consultarServiciosUseCase,
            EliminarServicioUseCase eliminarServicioUseCase
    ) {
        this.crearServicioUseCase = Objects.requireNonNull(crearServicioUseCase, "crearServicioUseCase no puede ser nulo");
        this.actualizarServicioUseCase = actualizarServicioUseCase;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.consultarServiciosUseCase = consultarServiciosUseCase;
        this.eliminarServicioUseCase = eliminarServicioUseCase;
    }

    // Constructor de compatibilidad para tests existentes que instancian solo CrearServicioUseCase
    public ServicioController(CrearServicioUseCase crearServicioUseCase) {
        this(crearServicioUseCase, null, null, null, null);
    }

    /**
     * Crear servicio — solo PROVEEDOR autenticado.
     * Anti-IDOR: el idProveedor se resuelve desde el contexto de autenticación,
     * ignorando cualquier valor que pudiera venir en el body.
     */
    @PreAuthorize("hasRole('PROVEEDOR')")
    @PostMapping
    public ResponseEntity<ServicioResponse> crearServicio(
            @Valid @RequestBody CrearServicioRequest request,
            UriComponentsBuilder uriComponentsBuilder,
            Authentication authentication
    ) {
        // Resolver idProveedor desde el token para prevenir IDOR
        Integer idProveedorAutenticado = extraerIdProveedor(authentication);
        CrearServicioRequest requestSeguro = resolverIdProveedor(request, idProveedorAutenticado);

        ServicioResponse response = crearServicioUseCase.ejecutar(requestSeguro);

        URI location = uriComponentsBuilder
                .path("/api/v1/servicios/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /** Consulta individual — pública (catálogo). */
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> obtenerServicioPorId(@PathVariable Integer id) {
        ServicioResponse response = obtenerServicioPorIdUseCase.ejecutar(id);
        return ResponseEntity.ok(response);
    }

    /** Listado paginado — público (catálogo). */
    @GetMapping
    public ResponseEntity<PaginaResponse<ServicioResponse>> consultarServicios(
            @RequestParam(required = false) Integer idProveedor,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginaResponse<ServicioResponse> response = consultarServiciosUseCase.ejecutar(idProveedor, nombre, page, size);
        return ResponseEntity.ok(response);
    }

    /** Actualizar servicio — ADMIN siempre; PROVEEDOR solo si es propietario. */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @servicioSecurity.esPropietario(authentication, #id))")
    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizarServicio(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarServicioRequest request
    ) {
        ServicioResponse response = actualizarServicioUseCase.ejecutar(id, request);
        return ResponseEntity.ok(response);
    }

    /** Eliminar servicio — ADMIN siempre; PROVEEDOR solo si es propietario. */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @servicioSecurity.esPropietario(authentication, #id))")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Integer id) {
        eliminarServicioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private Integer extraerIdProveedor(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
            Long id = principal.getIdUsuario();
            return id != null ? id.intValue() : null;
        }
        return null;
    }

    /**
     * Devuelve el mismo request o uno nuevo con el idProveedor corregido.
     * Si CrearServicioRequest es un record con idProveedor, construye uno nuevo;
     * en caso contrario, el use case deberá aceptar el valor del token.
     */
    private CrearServicioRequest resolverIdProveedor(CrearServicioRequest original, Integer idProveedorAutenticado) {
        if (idProveedorAutenticado == null) {
            return original;
        }
        // Reconstruir el request con el idProveedor del token (previene IDOR)
        return new CrearServicioRequest(
                idProveedorAutenticado,
                original.nombre(),
                original.duracionMinutos()
        );
    }
}

