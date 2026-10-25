package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.AsociarRecursoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.AsociarRecursoAServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarRecursosDeServicioUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.QuitarRecursoDeServicioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioRecursoControllerTest {

    @Mock private AsociarRecursoAServicioUseCase asociarRecursoAServicioUseCase;
    @Mock private ListarRecursosDeServicioUseCase listarRecursosDeServicioUseCase;
    @Mock private QuitarRecursoDeServicioUseCase quitarRecursoDeServicioUseCase;

    @InjectMocks private ServicioRecursoController controller;

    @Test
    @DisplayName("Asociar usa la cantidad enviada")
    void linksWithGivenQuantity() {
        // Arrange
        when(asociarRecursoAServicioUseCase.ejecutar(new AsociarRecursoComando(10, 3, 2)))
                .thenReturn(ServicioRecurso.reconstruir(4, 10, 3, 2));

        // Act
        ServicioRecursoResponseDTO response = controller.asociar(10, new AsociarRecursoRequestDTO(3, 2));

        // Assert
        assertEquals(new ServicioRecursoResponseDTO(4, 10, 3, 2), response);
    }

    @Test
    @DisplayName("Asociar usa una unidad cuando no se envía cantidad")
    void linksWithDefaultQuantity() {
        // Arrange
        when(asociarRecursoAServicioUseCase.ejecutar(new AsociarRecursoComando(10, 3, 1)))
                .thenReturn(ServicioRecurso.reconstruir(4, 10, 3, 1));

        // Act
        ServicioRecursoResponseDTO response = controller.asociar(10, new AsociarRecursoRequestDTO(3, null));

        // Assert
        assertEquals(1, response.cantidad());
    }

    @Test
    @DisplayName("Listar devuelve los recursos asociados al servicio")
    void listsLinkedResources() {
        when(listarRecursosDeServicioUseCase.ejecutar(10)).thenReturn(List.of(ServicioRecurso.reconstruir(4, 10, 3, 1)));

        assertEquals(List.of(new ServicioRecursoResponseDTO(4, 10, 3, 1)), controller.listar(10));
    }

    @Test
    @DisplayName("Quitar delega en el caso de uso")
    void removesLink() {
        controller.quitar(10, 3);

        verify(quitarRecursoDeServicioUseCase).ejecutar(10, 3);
    }
}
