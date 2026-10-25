package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarBloqueoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarBloqueosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarBloqueoRecursoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloqueoRecursoControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 8, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(2);

    @Mock private RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase;
    @Mock private ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase;
    @Mock private EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase;

    @InjectMocks private BloqueoRecursoController controller;

    private static BloqueoRecurso block() {
        return BloqueoRecurso.reconstruir(8, 1, INICIO, FIN, "Mantenimiento", INICIO.minusDays(1));
    }

    @Test
    @DisplayName("Registrar bloqueo devuelve el bloqueo guardado")
    void registersBlock() {
        // Arrange
        when(registrarBloqueoRecursoUseCase.ejecutar(new RegistrarBloqueoComando(1, INICIO, FIN, "Mantenimiento")))
                .thenReturn(block());

        // Act
        BloqueoRecursoResponseDTO response = controller.registrarBloqueo(1,
                new RegistrarBloqueoRequestDTO(INICIO, FIN, "Mantenimiento"));

        // Assert
        assertEquals(new BloqueoRecursoResponseDTO(8, 1, INICIO, FIN, "Mantenimiento", INICIO.minusDays(1)), response);
    }

    @Test
    @DisplayName("Listar bloqueos devuelve la página de bloqueos del recurso")
    void listsBlocks() {
        // Arrange
        when(listarBloqueosRecursoUseCase.ejecutar(1, 0, 20)).thenReturn(new ResultadoPaginado<>(List.of(block()), 0, 20, 1L));

        // Act
        PaginaResponseDTO<BloqueoRecursoResponseDTO> pagina = controller.listarBloqueos(1, 0, 20);

        // Assert
        assertEquals(8, pagina.contenido().get(0).id());
        assertEquals(1L, pagina.totalElementos());
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    @DisplayName("Eliminar bloqueo delega en el caso de uso")
    void deletesBlock() {
        controller.eliminarBloqueo(1, 8);

        verify(eliminarBloqueoRecursoUseCase).ejecutar(1, 8);
    }
}
