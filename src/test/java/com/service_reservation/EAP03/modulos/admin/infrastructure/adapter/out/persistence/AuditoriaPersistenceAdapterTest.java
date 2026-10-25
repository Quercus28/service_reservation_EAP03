package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity.RegistroAuditoriaEntity;
import com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.repository.RegistroAuditoriaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditoriaPersistenceAdapterTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 17, 10, 0);

    @Mock private RegistroAuditoriaJpaRepository repository;

    @InjectMocks private AuditoriaPersistenceAdapter adapter;

    private RegistroAuditoriaEntity savedEntity() {
        ArgumentCaptor<RegistroAuditoriaEntity> captor = ArgumentCaptor.forClass(RegistroAuditoriaEntity.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Guarda todos los datos del registro de auditoría")
    void savesAuditEntry() {
        // Act
        adapter.registrar(new RegistroAuditoria(FECHA, 1L, 42L, AccionAdmin.REVOCACION_INICIADA, RolOperativo.PROVEEDOR,
                ResultadoAuditoria.EXITO, "POST /api/admin/permisos/revocar", "reservasActivas=2"));

        // Assert
        RegistroAuditoriaEntity entidad = savedEntity();
        assertNull(entidad.getId());
        assertEquals(FECHA, entidad.getFecha());
        assertEquals(1L, entidad.getIdActor());
        assertEquals(42L, entidad.getIdUsuarioAfectado());
        assertEquals("REVOCACION_INICIADA", entidad.getAccion());
        assertEquals("PROVEEDOR", entidad.getRol());
        assertEquals("EXITO", entidad.getResultado());
        assertEquals("POST /api/admin/permisos/revocar", entidad.getEndpoint());
        assertEquals("reservasActivas=2", entidad.getDetalle());
    }

    @Test
    @DisplayName("Guarda el rol como nulo cuando la acción no involucra un rol")
    void savesNullRole() {
        adapter.registrar(new RegistroAuditoria(FECHA, 7L, null, AccionAdmin.ACCESO_DENEGADO, null,
                ResultadoAuditoria.DENEGADO, "GET /api/admin/solicitudes", "motivo=sin ROLE_ADMIN"));

        RegistroAuditoriaEntity entidad = savedEntity();
        assertNull(entidad.getRol());
        assertEquals("DENEGADO", entidad.getResultado());
    }
}
