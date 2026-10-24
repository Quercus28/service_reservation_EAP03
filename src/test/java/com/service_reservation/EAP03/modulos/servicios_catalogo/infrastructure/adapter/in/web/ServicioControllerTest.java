package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.config.GlobalExceptionHandler;
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
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CrearServicioUseCase crearServicioUseCase;

    @Mock
    private ActualizarServicioUseCase actualizarServicioUseCase;

    @Mock
    private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;

    @Mock
    private ConsultarServiciosUseCase consultarServiciosUseCase;

    @Mock
    private EliminarServicioUseCase eliminarServicioUseCase;

    @Mock
    private ConsultarProveedorUseCase consultarProveedorUseCase;

    @BeforeEach
    void setUp() {
        ServicioController controller = new ServicioController(
                crearServicioUseCase, actualizarServicioUseCase, obtenerServicioPorIdUseCase,
                consultarServiciosUseCase, eliminarServicioUseCase, consultarProveedorUseCase);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ServicioExceptionHandler(), new GlobalExceptionHandler(null))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 201 Created cuando la petición es válida")
    void debeCrearServicioRetornando201() throws Exception {
        ServicioResponse response = new ServicioResponse(1, 5, "Cambio de Aceite", 45, OffsetDateTime.now());

        // El usuario 10 tiene el perfil de proveedor 5
        UsuarioAutenticadoPrincipal principal =
                new UsuarioAutenticadoPrincipal(10L, "proveedor@test.com", List.of("PROVEEDOR"));
        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));

        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(10L)).thenReturn(Optional.of(5));
        when(crearServicioUseCase.ejecutar(any(CrearServicioRequest.class))).thenReturn(response);

        // El idProveedor del body (99) se ignora: manda el del proveedor autenticado (5)
        String jsonValido = """
                {
                    "idProveedor": 99,
                    "nombre": "Cambio de Aceite",
                    "duracionMinutos": 45
                }
                """;

        mockMvc.perform(post("/api/v1/servicios")
                .principal(autenticacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/servicios/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idProveedor").value(5))
                .andExpect(jsonPath("$.nombre").value("Cambio de Aceite"))
                .andExpect(jsonPath("$.duracionMinutos").value(45));

        verify(crearServicioUseCase).ejecutar(argThat(req -> Integer.valueOf(5).equals(req.idProveedor())));
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 400 Bad Request cuando faltan campos o son inválidos")
    void debeRetornar400ConCamposInvalidos() throws Exception {
        String jsonInvalido = """
                {
                    "idProveedor": null,
                    "nombre": "",
                    "duracionMinutos": 0
                }
                """;

        mockMvc.perform(post("/api/v1/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.details.idProveedor").exists())
                .andExpect(jsonPath("$.details.nombre").exists())
                .andExpect(jsonPath("$.details.duracionMinutos").exists());
    }

    private static final String JSON_CREAR = """
            {
                "idProveedor": 5,
                "nombre": "Cambio de Aceite",
                "duracionMinutos": 45
            }
            """;

    @Test
    @DisplayName("El constructor debe rechazar un CrearServicioUseCase nulo")
    void constructorRechazaCrearServicioUseCaseNulo() {
        assertThrows(NullPointerException.class, () -> new ServicioController(null));
    }

    @Test
    @DisplayName("El constructor de compatibilidad debe crear el controlador solo con CrearServicioUseCase")
    void constructorDeCompatibilidadCreaControlador() {
        assertNotNull(new ServicioController(crearServicioUseCase));
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 403 si no hay usuario autenticado")
    void crearSinAutenticacionRetorna403() throws Exception {
        mockMvc.perform(post("/api/v1/servicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_CREAR))
                .andExpect(status().isForbidden());

        verifyNoInteractions(crearServicioUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 403 si el principal no es un usuario autenticado del sistema")
    void crearConPrincipalDesconocidoRetorna403() throws Exception {
        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                "anonimo", null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));

        mockMvc.perform(post("/api/v1/servicios")
                .principal(autenticacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_CREAR))
                .andExpect(status().isForbidden());

        verifyNoInteractions(crearServicioUseCase, consultarProveedorUseCase);
    }

    @Test
    @DisplayName("POST /api/v1/servicios debe retornar 403 si el usuario no tiene perfil de proveedor")
    void crearSinPerfilProveedorRetorna403() throws Exception {
        UsuarioAutenticadoPrincipal principal =
                new UsuarioAutenticadoPrincipal(11L, "cliente@test.com", List.of("PROVEEDOR"));
        UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PROVEEDOR")));
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(11L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/servicios")
                .principal(autenticacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_CREAR))
                .andExpect(status().isForbidden());

        verifyNoInteractions(crearServicioUseCase);
    }

    @Test
    @DisplayName("GET /api/v1/servicios/{id} debe retornar 200 con el servicio")
    void obtenerPorIdRetorna200() throws Exception {
        when(obtenerServicioPorIdUseCase.ejecutar(7))
                .thenReturn(new ServicioResponse(7, 5, "Corte", 30, null));

        mockMvc.perform(get("/api/v1/servicios/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.idProveedor").value(5))
                .andExpect(jsonPath("$.nombre").value("Corte"))
                .andExpect(jsonPath("$.duracionMinutos").value(30));
    }

    @Test
    @DisplayName("GET /api/v1/servicios/{id} debe retornar 404 cuando el servicio no existe")
    void obtenerPorIdInexistenteRetorna404() throws Exception {
        when(obtenerServicioPorIdUseCase.ejecutar(99)).thenThrow(new ServicioNoEncontradoException(99));

        mockMvc.perform(get("/api/v1/servicios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SERVICIO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.message").value("No se encontró el servicio con ID: 99"));
    }

    @Test
    @DisplayName("GET /api/v1/servicios debe usar página 0 y tamaño 10 por defecto")
    void consultarUsaPaginacionPorDefecto() throws Exception {
        when(consultarServiciosUseCase.ejecutar(null, null, 0, 10))
                .thenReturn(PaginaResponse.de(List.of(), 0, 10, 0L, 0));

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.esPrimera").value(true))
                .andExpect(jsonPath("$.esUltima").value(true));

        verify(consultarServiciosUseCase).ejecutar(null, null, 0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/servicios debe pasar filtros y paginación al caso de uso")
    void consultarPasaFiltrosYPaginacion() throws Exception {
        ServicioResponse servicio = new ServicioResponse(1, 5, "Corte", 30, null);
        when(consultarServiciosUseCase.ejecutar(5, "cor", 2, 3))
                .thenReturn(PaginaResponse.de(List.of(servicio), 2, 3, 7L, 3));

        mockMvc.perform(get("/api/v1/servicios")
                .param("idProveedor", "5")
                .param("nombre", "cor")
                .param("page", "2")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(1))
                .andExpect(jsonPath("$.pagina").value(2))
                .andExpect(jsonPath("$.tamano").value(3))
                .andExpect(jsonPath("$.totalElementos").value(7))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.esUltima").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/servicios/{id} debe retornar 200 con el servicio actualizado")
    void actualizarRetorna200() throws Exception {
        when(actualizarServicioUseCase.ejecutar(eq(7), any(ActualizarServicioRequest.class)))
                .thenReturn(new ServicioResponse(7, 5, "Corte y barba", 45, null));

        mockMvc.perform(put("/api/v1/servicios/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Corte y barba",
                            "duracionMinutos": 45
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Corte y barba"))
                .andExpect(jsonPath("$.duracionMinutos").value(45));

        verify(actualizarServicioUseCase).ejecutar(7, new ActualizarServicioRequest("Corte y barba", 45));
    }

    @Test
    @DisplayName("PUT /api/v1/servicios/{id} debe retornar 400 cuando el body es inválido")
    void actualizarConBodyInvalidoRetorna400() throws Exception {
        mockMvc.perform(put("/api/v1/servicios/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "",
                            "duracionMinutos": 0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.details.nombre").exists())
                .andExpect(jsonPath("$.details.duracionMinutos").exists());

        verifyNoInteractions(actualizarServicioUseCase);
    }

    @Test
    @DisplayName("PUT /api/v1/servicios/{id} debe retornar 400 cuando el dominio rechaza los datos")
    void actualizarConErrorDeDominioRetorna400() throws Exception {
        when(actualizarServicioUseCase.ejecutar(eq(7), any(ActualizarServicioRequest.class)))
                .thenThrow(new ServicioInvalidoException("El nombre del servicio no puede exceder los 150 caracteres."));

        mockMvc.perform(put("/api/v1/servicios/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "nombre": "Corte",
                            "duracionMinutos": 30
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SERVICIO_INVALIDO"));
    }

    @Test
    @DisplayName("DELETE /api/v1/servicios/{id} debe retornar 204 No Content")
    void eliminarRetorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/servicios/7"))
                .andExpect(status().isNoContent());

        verify(eliminarServicioUseCase).eliminar(7);
    }

    @Test
    @DisplayName("DELETE /api/v1/servicios/{id} debe retornar 404 cuando el servicio no existe")
    void eliminarInexistenteRetorna404() throws Exception {
        doThrow(new ServicioNoEncontradoException(99)).when(eliminarServicioUseCase).eliminar(99);

        mockMvc.perform(delete("/api/v1/servicios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SERVICIO_NO_ENCONTRADO"));
    }
}
