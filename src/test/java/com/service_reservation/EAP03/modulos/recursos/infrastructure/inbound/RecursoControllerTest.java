package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ActualizarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.CrearRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ActualizarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarRecursosUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.CrearRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.DesactivarRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ObtenerRecursoPorIdUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
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
    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime HASTA = DESDE.plusHours(2);

    @Mock private CrearRecursoUseCase crearRecursoUseCase;
    @Mock private ActualizarRecursoUseCase actualizarRecursoUseCase;
    @Mock private ConsultarRecursosUseCase consultarRecursosUseCase;
    @Mock private ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase;
    @Mock private DesactivarRecursoUseCase desactivarRecursoUseCase;
    @Mock private ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    @Mock private RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;
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

    @Test
    @DisplayName("Consultar disponibilidad devuelve el cálculo del caso de uso")
    void queriesAvailability() {
        // Arrange
        when(consultarDisponibilidadUseCase.ejecutar(1, DESDE, HASTA))
                .thenReturn(new DisponibilidadRecurso(1, DESDE, HASTA, 5, 2, 3, true));

        // Act
        DisponibilidadResponseDTO response = controller.consultarDisponibilidad(1, DESDE, HASTA);

        // Assert
        assertEquals(new DisponibilidadResponseDTO(1, DESDE, HASTA, 5, 2, 3, true), response);
    }

    @Test
    @DisplayName("Registrar préstamo devuelve el préstamo guardado")
    void registersLoan() {
        // Arrange
        RegistrarPrestamoComando comando = new RegistrarPrestamoComando(1, 100, 2, DESDE, HASTA, PRECIO);
        when(registrarPrestamoRecursoUseCase.ejecutar(comando))
                .thenReturn(RecursoPrestado.reconstruir(4, 1, 100, 2, DESDE, HASTA, PRECIO, null));

        // Act
        RecursoPrestadoResponseDTO response = controller.registrarPrestamo(1,
                new RegistrarPrestamoRequestDTO(100, 2, DESDE, HASTA, PRECIO));

        // Assert
        assertEquals(new RecursoPrestadoResponseDTO(4, 1, 100, 2, DESDE, HASTA, PRECIO), response);
    }
}
