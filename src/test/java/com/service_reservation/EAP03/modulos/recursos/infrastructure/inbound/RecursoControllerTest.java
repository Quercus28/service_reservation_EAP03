package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ActualizarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.CrearRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ActualizarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarRecursosUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.CrearRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.DesactivarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ObtenerRecursoPorIdUseCase;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.config.RecursoSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoControllerTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");
    private static final LocalDateTime CREADO = LocalDateTime.of(2026, 9, 1, 8, 0);

    @Mock private CrearRecursoUseCase crearRecursoUseCase;
    @Mock private ActualizarRecursoUseCase actualizarRecursoUseCase;
    @Mock private ConsultarRecursosUseCase consultarRecursosUseCase;
    @Mock private ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase;
    @Mock private DesactivarRecursoUseCase desactivarRecursoUseCase;
    @Mock private RecursoSecurity recursoSecurity;
    @Mock private Authentication authentication;

    @InjectMocks private RecursoController controller;

    private static Recurso resource() {
        return Recurso.reconstruir(1, 7, "Camilla", PRECIO, 3, CREADO, null);
    }

    @Test
    @DisplayName("Crear registra el recurso para el proveedor autenticado")
    void createsResourceForAuthenticatedProvider() {
        // Arrange
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.of(7));
        when(crearRecursoUseCase.ejecutar(new CrearRecursoComando(7, "Camilla", PRECIO, 3))).thenReturn(resource());

        // Act
        RecursoResponseDTO response = controller.crear(new CrearRecursoRequestDTO("Camilla", PRECIO, 3), authentication);

        // Assert
        assertEquals(new RecursoResponseDTO(1, 7, "Camilla", PRECIO, 3, CREADO, true), response);
    }

    @Test
    @DisplayName("Crear falla si el usuario no tiene perfil de proveedor")
    void createFailsWithoutProviderProfile() {
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.empty());
        CrearRecursoRequestDTO request = new CrearRecursoRequestDTO("Camilla", PRECIO, 3);

        assertThrows(DatosInvalidosException.class, () -> controller.crear(request, authentication));
        verifyNoInteractions(crearRecursoUseCase);
    }

    @Test
    @DisplayName("Listar devuelve la página de recursos del proveedor autenticado")
    void listsProviderResources() {
        // Arrange
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.of(7));
        when(consultarRecursosUseCase.ejecutar(7, 1, 2)).thenReturn(new ResultadoPaginado<>(List.of(resource()), 1, 2, 3L));

        // Act
        PaginaResponseDTO<RecursoResponseDTO> pagina = controller.listar(1, 2, authentication);

        // Assert
        assertEquals(1, pagina.contenido().size());
        assertEquals("Camilla", pagina.contenido().get(0).nombre());
        assertEquals(1, pagina.pagina());
        assertEquals(2, pagina.tamano());
        assertEquals(3L, pagina.totalElementos());
        assertEquals(2, pagina.totalPaginas());
    }

    @Test
    @DisplayName("Listar falla si el usuario no tiene perfil de proveedor")
    void listFailsWithoutProviderProfile() {
        when(recursoSecurity.idProveedorDe(authentication)).thenReturn(Optional.empty());

        assertThrows(DatosInvalidosException.class, () -> controller.listar(0, 20, authentication));
        verifyNoInteractions(consultarRecursosUseCase);
    }

    @Test
    @DisplayName("Obtener devuelve el recurso pedido")
    void getsResource() {
        when(obtenerRecursoPorIdUseCase.ejecutar(1)).thenReturn(resource());

        assertEquals(1, controller.obtener(1).id());
    }

    @Test
    @DisplayName("Actualizar pasa los datos nuevos al caso de uso")
    void updatesResource() {
        // Arrange
        Recurso actualizado = Recurso.reconstruir(1, 7, "Camilla grande", BigDecimal.ONE, 5, CREADO, null);
        when(actualizarRecursoUseCase.ejecutar(new ActualizarRecursoComando(1, "Camilla grande", BigDecimal.ONE, 5)))
                .thenReturn(actualizado);

        // Act
        RecursoResponseDTO response = controller.actualizar(1, new ActualizarRecursoRequestDTO("Camilla grande", BigDecimal.ONE, 5));

        // Assert
        assertEquals("Camilla grande", response.nombre());
        assertEquals(5, response.stock());
    }

    @Test
    @DisplayName("Desactivar delega en el caso de uso")
    void deactivatesResource() {
        controller.desactivar(1);

        verify(desactivarRecursoUseCase).ejecutar(1);
    }
}
