package com.service_reservation.EAP03.modulos.servicios_catalogo.application.service;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCatalogoServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    private ServicioCatalogoServiceImpl servicioCatalogoService;

    @BeforeEach
    void setUp() {
        servicioCatalogoService = new ServicioCatalogoServiceImpl(servicioRepository);
    }

    @Test
    @DisplayName("Debe ejecutar el caso de uso y guardar el servicio correctamente")
    void debeEjecutarCasoDeUsoCrearServicio() {
        CrearServicioRequest request = new CrearServicioRequest(10, "Mantenimiento Preventivo", 90);

        OffsetDateTime fechaSimulada = OffsetDateTime.now();
        Servicio servicioGuardadoMock = Servicio.reconstituir(100, 10, "Mantenimiento Preventivo", 90, fechaSimulada);

        when(servicioRepository.guardar(any(Servicio.class))).thenReturn(servicioGuardadoMock);

        ServicioResponse response = servicioCatalogoService.ejecutar(request);

        assertNotNull(response);
        assertEquals(100, response.id());
        assertEquals(10, response.idProveedor());
        assertEquals("Mantenimiento Preventivo", response.nombre());
        assertEquals(90, response.duracionMinutos());
        assertEquals(fechaSimulada, response.createdAt());

        ArgumentCaptor<Servicio> captor = ArgumentCaptor.forClass(Servicio.class);
        verify(servicioRepository, times(1)).guardar(captor.capture());

        Servicio servicioEnviado = captor.getValue();
        assertEquals(10, servicioEnviado.getIdProveedor());
        assertEquals("Mantenimiento Preventivo", servicioEnviado.getNombre());
        assertEquals(90, servicioEnviado.getDuracionMinutos());
        assertNull(servicioEnviado.getId());
    }

    @Test
    @DisplayName("El constructor debe rechazar un repositorio nulo")
    void constructorRechazaRepositorioNulo() {
        assertThrows(NullPointerException.class, () -> new ServicioCatalogoServiceImpl(null));
    }

    // ---------------------------------------------------------------- Crear

    @Test
    @DisplayName("Crear debe rechazar un request nulo sin tocar el repositorio")
    void crearRechazaRequestNulo() {
        // Act
        assertThrows(NullPointerException.class, () -> servicioCatalogoService.ejecutar((CrearServicioRequest) null));

        // Assert
        verifyNoInteractions(servicioRepository);
    }

    @Test
    @DisplayName("Crear debe propagar el error de dominio y no guardar un servicio inválido")
    void crearNoGuardaServicioInvalido() {
        // Arrange
        CrearServicioRequest request = new CrearServicioRequest(10, "Corte", 0);

        // Act
        assertThrows(ServicioInvalidoException.class, () -> servicioCatalogoService.ejecutar(request));

        // Assert
        verify(servicioRepository, never()).guardar(any());
    }

    // ---------------------------------------------------------------- Actualizar

    @Test
    @DisplayName("Actualizar debe modificar el servicio existente y guardarlo")
    void actualizarModificaYGuarda() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.now();
        Servicio existente = Servicio.reconstituir(7, 10, "Corte", 30, creado);
        when(servicioRepository.buscarPorId(7)).thenReturn(Optional.of(existente));
        when(servicioRepository.guardar(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ServicioResponse response = servicioCatalogoService.ejecutar(7, new ActualizarServicioRequest("Corte y barba", 45));

        // Assert
        assertEquals(7, response.id());
        assertEquals(10, response.idProveedor());
        assertEquals("Corte y barba", response.nombre());
        assertEquals(45, response.duracionMinutos());
        assertEquals(creado, response.createdAt());
        verify(servicioRepository).guardar(existente);
    }

    @Test
    @DisplayName("Actualizar debe lanzar ServicioNoEncontradoException si el servicio no existe")
    void actualizarServicioInexistente() {
        // Arrange
        when(servicioRepository.buscarPorId(99)).thenReturn(Optional.empty());
        ActualizarServicioRequest request = new ActualizarServicioRequest("Corte", 30);

        // Act
        ServicioNoEncontradoException ex = assertThrows(
                ServicioNoEncontradoException.class,
                () -> servicioCatalogoService.ejecutar(99, request)
        );

        // Assert
        assertTrue(ex.getMessage().contains("99"));
        verify(servicioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Actualizar debe rechazar id o request nulos")
    void actualizarRechazaParametrosNulos() {
        // Arrange
        ActualizarServicioRequest request = new ActualizarServicioRequest("Corte", 30);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> servicioCatalogoService.ejecutar(null, request));
        assertThrows(NullPointerException.class, () -> servicioCatalogoService.ejecutar(7, null));
        verifyNoInteractions(servicioRepository);
    }

    @Test
    @DisplayName("Actualizar con datos inválidos no debe guardar cambios")
    void actualizarConDatosInvalidosNoGuarda() {
        // Arrange
        Servicio existente = Servicio.reconstituir(7, 10, "Corte", 30, OffsetDateTime.now());
        when(servicioRepository.buscarPorId(7)).thenReturn(Optional.of(existente));
        ActualizarServicioRequest request = new ActualizarServicioRequest("", 30);

        // Act
        assertThrows(ServicioInvalidoException.class, () -> servicioCatalogoService.ejecutar(7, request));

        // Assert
        verify(servicioRepository, never()).guardar(any());
    }

    // ---------------------------------------------------------------- Obtener por id

    @Test
    @DisplayName("Obtener por id debe devolver el servicio encontrado")
    void obtenerPorIdDevuelveServicio() {
        // Arrange
        Servicio existente = Servicio.reconstituir(7, 10, "Corte", 30, null);
        when(servicioRepository.buscarPorId(7)).thenReturn(Optional.of(existente));

        // Act
        ServicioResponse response = servicioCatalogoService.ejecutar(7);

        // Assert
        assertEquals(7, response.id());
        assertEquals("Corte", response.nombre());
    }

    @Test
    @DisplayName("Obtener por id debe lanzar ServicioNoEncontradoException si no existe")
    void obtenerPorIdInexistente() {
        // Arrange
        when(servicioRepository.buscarPorId(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServicioNoEncontradoException.class, () -> servicioCatalogoService.ejecutar(99));
    }

    @Test
    @DisplayName("Obtener por id nulo debe lanzar ServicioNoEncontradoException sin consultar el repositorio")
    void obtenerPorIdNulo() {
        // Act
        ServicioNoEncontradoException ex = assertThrows(
                ServicioNoEncontradoException.class,
                () -> servicioCatalogoService.ejecutar((Integer) null)
        );

        // Assert
        assertEquals("El id del servicio es obligatorio", ex.getMessage());
        verifyNoInteractions(servicioRepository);
    }

    // ---------------------------------------------------------------- Consultar paginado

    @Test
    @DisplayName("Consultar debe pasar los filtros y mapear el resultado paginado")
    void consultarMapeaResultadoPaginado() {
        // Arrange
        Servicio s1 = Servicio.reconstituir(1, 10, "Corte", 30, null);
        Servicio s2 = Servicio.reconstituir(2, 10, "Tinte", 90, null);
        when(servicioRepository.buscarPaginado(10, "co", 1, 2))
                .thenReturn(new ResultadoPaginado<>(List.of(s1, s2), 1, 2, 5L, 3));

        // Act
        PaginaResponse<ServicioResponse> response = servicioCatalogoService.ejecutar(10, "co", 1, 2);

        // Assert
        assertEquals(List.of(1, 2), response.contenido().stream().map(ServicioResponse::id).toList());
        assertEquals(1, response.pagina());
        assertEquals(2, response.tamano());
        assertEquals(5L, response.totalElementos());
        assertEquals(3, response.totalPaginas());
        assertFalse(response.esPrimera());
        assertFalse(response.esUltima());
    }

    @ParameterizedTest(name = "pagina={0}, tamano={1} -> pagina={2}, tamano={3}")
    @CsvSource({
            "-1, 10, 0, 10",
            "-5, 0, 0, 10",
            "0, -3, 0, 10",
            "2, 51, 2, 50",
            "0, 50, 0, 50",
            "3, 1, 3, 1"
    })
    @DisplayName("Consultar debe normalizar página negativa y tamaño fuera de rango")
    void consultarNormalizaPaginacion(int pagina, int tamano, int paginaEsperada, int tamanoEsperado) {
        // Arrange
        when(servicioRepository.buscarPaginado(null, null, paginaEsperada, tamanoEsperado))
                .thenReturn(new ResultadoPaginado<>(List.of(), paginaEsperada, tamanoEsperado, 0L, 0));

        // Act
        PaginaResponse<ServicioResponse> response = servicioCatalogoService.ejecutar(null, null, pagina, tamano);

        // Assert
        verify(servicioRepository).buscarPaginado(null, null, paginaEsperada, tamanoEsperado);
        assertTrue(response.contenido().isEmpty());
        assertTrue(response.esUltima());
    }

    // ---------------------------------------------------------------- Eliminar

    @Test
    @DisplayName("Eliminar debe borrar el servicio cuando existe")
    void eliminarServicioExistente() {
        // Arrange
        when(servicioRepository.existePorId(7)).thenReturn(true);

        // Act
        servicioCatalogoService.eliminar(7);

        // Assert
        verify(servicioRepository).eliminarPorId(7);
    }

    @Test
    @DisplayName("Eliminar debe lanzar ServicioNoEncontradoException si el servicio no existe")
    void eliminarServicioInexistente() {
        // Arrange
        when(servicioRepository.existePorId(99)).thenReturn(false);

        // Act
        assertThrows(ServicioNoEncontradoException.class, () -> servicioCatalogoService.eliminar(99));

        // Assert
        verify(servicioRepository, never()).eliminarPorId(any());
    }

    @Test
    @DisplayName("Eliminar con id nulo debe lanzar ServicioNoEncontradoException sin consultar el repositorio")
    void eliminarConIdNulo() {
        // Act
        assertThrows(ServicioNoEncontradoException.class, () -> servicioCatalogoService.eliminar(null));

        // Assert
        verifyNoInteractions(servicioRepository);
    }
}
