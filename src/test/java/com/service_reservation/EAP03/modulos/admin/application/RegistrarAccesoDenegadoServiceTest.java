package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrarAccesoDenegadoServiceTest {

    @Mock private AuditoriaPort auditoriaPort;

    @InjectMocks private RegistrarAccesoDenegadoService service;

    @Test
    @DisplayName("Registra el intento como acceso denegado con método y ruta")
    void recordsDeniedAccess() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 17, 10, 0);

        // Act
        service.registrar(new IntentoAccesoDenegado(fecha, 7L, "POST", "/api/admin/permisos/revocar"));

        // Assert
        verify(auditoriaPort).registrar(new RegistroAuditoria(fecha, 7L, null, AccionAdmin.ACCESO_DENEGADO, null,
                ResultadoAuditoria.DENEGADO, "POST /api/admin/permisos/revocar", "motivo=sin ROLE_ADMIN"));
    }
}
