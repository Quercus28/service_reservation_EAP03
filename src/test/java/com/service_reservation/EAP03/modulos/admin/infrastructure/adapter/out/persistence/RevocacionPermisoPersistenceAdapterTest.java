package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoRevocacion;
import com.service_reservation.EAP03.modulos.admin.domain.model.RevocacionPermiso;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RevocacionPermisoEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RevocacionPermisoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevocacionPermisoPersistenceAdapterTest {

    private static final LocalDateTime SOLICITUD = LocalDateTime.of(2026, 10, 17, 10, 0);
    private static final LocalDateTime LIMITE = SOLICITUD.plusDays(5);
    private static final LocalDateTime CIERRE = SOLICITUD.plusDays(6);

    @Mock private RevocacionPermisoJpaRepository repository;

    @InjectMocks private RevocacionPermisoPersistenceAdapter adapter;

    private static RevocacionPermisoEntity entity(Long id, String estado) {
        RevocacionPermisoEntity entidad = new RevocacionPermisoEntity();
        entidad.setId(id);
        entidad.setIdUsuario(42L);
        entidad.setRol("PROVEEDOR");
        entidad.setEstado(estado);
        entidad.setMotivo("Fraude");
        entidad.setIdAdmin(1L);
        entidad.setFechaSolicitud(SOLICITUD);
        entidad.setFechaLimite(LIMITE);
        entidad.setFechaCierre(CIERRE);
        entidad.setReservasCanceladas(2);
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte la revocación a entidad y devuelve la revocación guardada")
    void savesRevocation() {
        // Arrange
        when(repository.save(any())).thenReturn(entity(7L, "COMPLETADA"));
        RevocacionPermiso revocacion = new RevocacionPermiso(null, 42L, RolOperativo.PROVEEDOR, EstadoRevocacion.COMPLETADA,
                "Fraude", 1L, SOLICITUD, LIMITE, CIERRE, 2);

        // Act
        RevocacionPermiso guardada = adapter.guardar(revocacion);

        // Assert
        ArgumentCaptor<RevocacionPermisoEntity> captor = ArgumentCaptor.forClass(RevocacionPermisoEntity.class);
        verify(repository).save(captor.capture());
        RevocacionPermisoEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(42L, enviada.getIdUsuario());
        assertEquals("PROVEEDOR", enviada.getRol());
        assertEquals("COMPLETADA", enviada.getEstado());
        assertEquals("Fraude", enviada.getMotivo());
        assertEquals(1L, enviada.getIdAdmin());
        assertEquals(SOLICITUD, enviada.getFechaSolicitud());
        assertEquals(LIMITE, enviada.getFechaLimite());
        assertEquals(CIERRE, enviada.getFechaCierre());
        assertEquals(2, enviada.getReservasCanceladas());
        assertEquals(7L, guardada.getId());
        assertEquals(EstadoRevocacion.COMPLETADA, guardada.getEstado());
        assertEquals(2, guardada.getReservasCanceladas());
    }

    @Test
    @DisplayName("Busca la revocación pendiente del usuario para un rol")
    void findsPendingRevocation() {
        when(repository.findByIdUsuarioAndRolAndEstado(42L, "PROVEEDOR", "PENDIENTE_CANCELACION"))
                .thenReturn(Optional.of(entity(7L, "PENDIENTE_CANCELACION")));

        RevocacionPermiso pendiente = adapter.buscarPendiente(42L, RolOperativo.PROVEEDOR).orElseThrow();

        assertTrue(pendiente.estaPendiente());
    }

    @Test
    @DisplayName("Lista todas las revocaciones o solo las de un estado")
    void listsRevocations() {
        when(repository.findAll()).thenReturn(List.of(entity(7L, "COMPLETADA"), entity(8L, "ANULADA")));
        when(repository.findByEstado("ANULADA")).thenReturn(List.of(entity(8L, "ANULADA")));

        assertEquals(List.of(7L, 8L), adapter.listar(null).stream().map(RevocacionPermiso::getId).toList());
        assertEquals(List.of(8L), adapter.listar(EstadoRevocacion.ANULADA).stream().map(RevocacionPermiso::getId).toList());
    }
}
