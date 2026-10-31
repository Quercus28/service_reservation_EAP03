package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@Tag(name = "Servicios", description = "Catálogo de servicios y operaciones de administración del proveedor propietario.")
public class ServicioController {

    private final CrearServicioUseCase crearServicioUseCase;
    private final ActualizarServicioUseCase actualizarServicioUseCase;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;
    private final ConsultarServiciosUseCase consultarServiciosUseCase;
    private final EliminarServicioUseCase eliminarServicioUseCase;
    private final ConsultarProveedorUseCase consultarProveedorUseCase;

    @Autowired
    public ServicioController(
            CrearServicioUseCase crearServicioUseCase,
            ActualizarServicioUseCase actualizarServicioUseCase,
            ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase,
            ConsultarServiciosUseCase consultarServiciosUseCase,
            EliminarServicioUseCase eliminarServicioUseCase,
            ConsultarProveedorUseCase consultarProveedorUseCase
    ) {
        this.crearServicioUseCase = Objects.requireNonNull(crearServicioUseCase, "crearServicioUseCase no puede ser nulo");
        this.actualizarServicioUseCase = actualizarServicioUseCase;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
        this.consultarServiciosUseCase = consultarServiciosUseCase;
        this.eliminarServicioUseCase = eliminarServicioUseCase;
        this.consultarProveedorUseCase = consultarProveedorUseCase;
    }

    // Constructor de compatibilidad para tests existentes que instancian solo CrearServicioUseCase
    public ServicioController(CrearServicioUseCase crearServicioUseCase) {
        this(crearServicioUseCase, null, null, null, null, null);
    }

    /**
     * Crear servicio — solo PROVEEDOR autenticado.
     * Anti-IDOR: el idProveedor se resuelve desde el contexto de autenticación,
     * ignorando cualquier valor que pudiera venir en el body.
     */
    @PreAuthorize("hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR')")
    @PostMapping
    @Operation(
            summary = "Crear un servicio",
            description = "Registra un servicio para el proveedor autenticado. El idProveedor del cuerpo se reemplaza por el proveedor asociado al JWT; el cliente debe tener rol PROVEEDOR y permiso operativo activo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Servicio creado; la respuesta incluye la URI del recurso en Location."),
            @ApiResponse(responseCode = "400", description = "El nombre o la duración no cumplen las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene un perfil de proveedor o su permiso operativo está inactivo.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del servicio. idProveedor es obligatorio por el contrato de entrada, pero se toma del usuario autenticado.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "idProveedor": 5,
                      "nombre": "Consulta de fisioterapia",
                      "duracionMinutos": 60
                    }
                    """))
    )
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

    /** Consulta individual del catálogo — requiere sesión (cualquier usuario autenticado). */
    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un servicio",
            description = "Obtiene el detalle de un servicio por su identificador. Requiere un usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio encontrado."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ServicioResponse> obtenerServicioPorId(@PathVariable Integer id) {
        ServicioResponse response = obtenerServicioPorIdUseCase.ejecutar(id);
        return ResponseEntity.ok(response);
    }

    /** Listado paginado del catálogo — requiere sesión (cualquier usuario autenticado). */
    @GetMapping
    @Operation(
            summary = "Consultar el catálogo de servicios",
            description = "Devuelve una página de servicios y permite filtrar opcionalmente por proveedor o por nombre. Requiere un usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de servicios consultada."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<PaginaResponse<ServicioResponse>> consultarServicios(
            @io.swagger.v3.oas.annotations.Parameter(description = "Identificador del proveedor por el que se desea filtrar.")
            @RequestParam(required = false) Integer idProveedor,
            @io.swagger.v3.oas.annotations.Parameter(description = "Texto para filtrar por nombre del servicio.")
            @RequestParam(required = false) String nombre,
            @io.swagger.v3.oas.annotations.Parameter(description = "Índice de página, comenzando en 0.", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "Cantidad de resultados por página.", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        PaginaResponse<ServicioResponse> response = consultarServiciosUseCase.ejecutar(idProveedor, nombre, page, size);
        return ResponseEntity.ok(response);
    }

    /** Actualizar servicio — ADMIN siempre; PROVEEDOR solo si es propietario y tiene permiso operativo activo. */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR') and @servicioSecurity.esPropietario(authentication, #id))")
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un servicio",
            description = "Actualiza el nombre y la duración. Puede hacerlo un administrador o el proveedor propietario con permiso operativo activo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Servicio actualizado."),
            @ApiResponse(responseCode = "400", description = "El identificador o los datos no cumplen las validaciones.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para modificar este servicio.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevos valores para el nombre y la duración del servicio.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "nombre": "Consulta de fisioterapia",
                      "duracionMinutos": 60
                    }
                    """))
    )
    public ResponseEntity<ServicioResponse> actualizarServicio(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarServicioRequest request
    ) {
        ServicioResponse response = actualizarServicioUseCase.ejecutar(id, request);
        return ResponseEntity.ok(response);
    }

    /** Eliminar servicio — ADMIN siempre; PROVEEDOR solo si es propietario y tiene permiso operativo activo. */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROVEEDOR') and @permisoOperativo.activo(authentication, 'PROVEEDOR') and @servicioSecurity.esPropietario(authentication, #id))")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un servicio",
            description = "Elimina el servicio indicado. Puede hacerlo un administrador o el proveedor propietario con permiso operativo activo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Servicio eliminado; la respuesta no contiene cuerpo."),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para eliminar este servicio.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> eliminarServicio(@PathVariable Integer id) {
        eliminarServicioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Resuelve el id del PROVEEDOR (no el del usuario) a partir del usuario autenticado.
     * Si el usuario no tiene perfil de proveedor se niega el acceso (403): nunca se usa el
     * id de usuario ni el idProveedor del body como respaldo.
     */
    private Integer extraerIdProveedor(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
            return consultarProveedorUseCase.buscarIdProveedorPorUsuario(principal.getIdUsuario())
                    .orElseThrow(() -> new AccessDeniedException(
                            "El usuario autenticado no tiene un perfil de proveedor asociado"));
        }
        throw new AccessDeniedException("No se pudo identificar al proveedor autenticado");
    }

    /**
     * Devuelve un request nuevo con el idProveedor del proveedor autenticado,
     * ignorando el que venga en el body.
     */
    private CrearServicioRequest resolverIdProveedor(CrearServicioRequest original, Integer idProveedorAutenticado) {
        // Reconstruir el request con el idProveedor del token (previene IDOR)
        return new CrearServicioRequest(
                idProveedorAutenticado,
                original.nombre(),
                original.duracionMinutos()
        );
    }
}
