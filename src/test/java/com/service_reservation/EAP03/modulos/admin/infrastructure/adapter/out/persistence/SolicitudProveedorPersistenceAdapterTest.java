package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.EstadoSolicitud;
import com.service_reservation.EAP03.modulos.admin.domain.model.SolicitudAprobacionProveedor;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.SolicitudAprobacionProveedorEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.SolicitudAprobacionProveedorJpaRepository;
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
class SolicitudProveedorPersistenceAdapterTest {

    private static final LocalDateTime SOLICITUD = LocalDateTime.of(2026, 10, 17, 10, 0);
    private static final LocalDateTime RESOLUCION = SOLICITUD.plusDays(1);

    @Mock private SolicitudAprobacionProveedorJpaRepository repository;

    @InjectMocks private SolicitudProveedorPersistenceAdapter adapter;

    private static SolicitudAprobacionProveedorEntity entity(Long id, String estado) {
        SolicitudAprobacionProveedorEntity entidad = new SolicitudAprobacionProveedorEntity();
        entidad.setId(id);
        entidad.setIdUsuario(42L);
        entidad.setEstado(estado);
        entidad.setFechaSolicitud(SOLICITUD);
        entidad.setFechaResolucion(RESOLUCION);
        entidad.setIdAdminResolutor(1L);
        entidad.setMotivoRechazo("Documentos incompletos");
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte la solicitud a entidad y devuelve la solicitud guardada")
    void savesRequest() {
        // Arrange
        when(repository.save(any())).thenReturn(entity(50L, "RECHAZADA"));
        SolicitudAprobacionProveedor solicitud = new SolicitudAprobacionProveedor(null, 42L, EstadoSolicitud.RECHAZADA,
                SOLICITUD, RESOLUCION, 1L, "Documentos incompletos");

        // Act
        SolicitudAprobacionProveedor guardada = adapter.guardar(solicitud);

        // Assert
        ArgumentCaptor<SolicitudAprobacionProveedorEntity> captor = ArgumentCaptor.forClass(SolicitudAprobacionProveedorEntity.class);
        verify(repository).save(captor.capture());
        SolicitudAprobacionProveedorEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(42L, enviada.getIdUsuario());
        assertEquals("RECHAZADA", enviada.getEstado());
        assertEquals(SOLICITUD, enviada.getFechaSolicitud());
        assertEquals(RESOLUCION, enviada.getFechaResolucion());
        assertEquals(1L, enviada.getIdAdminResolutor());
        assertEquals("Documentos incompletos", enviada.getMotivoRechazo());
        assertEquals(50L, guardada.getId());
        assertEquals(EstadoSolicitud.RECHAZADA, guardada.getEstado());
        assertEquals("Documentos incompletos", guardada.getMotivoRechazo());
    }

    @Test
    @DisplayName("Busca una solicitud por id y la última de un usuario")
    void findsRequests() {
        when(repository.findById(50L)).thenReturn(Optional.of(entity(50L, "PENDIENTE")));
        when(repository.findTopByIdUsuarioOrderByFechaSolicitudDesc(42L)).thenReturn(Optional.of(entity(51L, "APROBADA")));

        assertEquals(EstadoSolicitud.PENDIENTE, adapter.buscarPorId(50L).orElseThrow().getEstado());
        assertEquals(51L, adapter.buscarUltimaPorUsuario(42L).orElseThrow().getId());
    }

    @Test
    @DisplayName("Consulta si el usuario tiene solicitudes pendientes o aprobadas")
    void checksPendingAndApprovedRequests() {
        when(repository.existsByIdUsuarioAndEstado(42L, "PENDIENTE")).thenReturn(true);
        when(repository.existsByIdUsuarioAndEstado(42L, "APROBADA")).thenReturn(false);

        assertTrue(adapter.existePendienteParaUsuario(42L));
        assertFalse(adapter.existeAprobadaParaUsuario(42L));
    }

    @Test
    @DisplayName("Lista todas las solicitudes o solo las de un estado")
    void listsRequests() {
        when(repository.findAll()).thenReturn(List.of(entity(50L, "PENDIENTE"), entity(51L, "APROBADA")));
        when(repository.findByEstado("APROBADA")).thenReturn(List.of(entity(51L, "APROBADA")));

        assertEquals(List.of(50L, 51L), adapter.listar(null).stream().map(SolicitudAprobacionProveedor::getId).toList());
        assertEquals(List.of(51L), adapter.listar(EstadoSolicitud.APROBADA).stream().map(SolicitudAprobacionProveedor::getId).toList());
    }
}
