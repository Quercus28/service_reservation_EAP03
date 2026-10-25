package com.service_reservation.EAP03.modulos.admin.infrastructure.events;

import com.service_reservation.EAP03.modulos.admin.domain.ports.in.GestionarSolicitudesProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.events.UsuarioRegistradoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegistroProveedorListenerTest {

    @Mock private GestionarSolicitudesProveedorUseCase gestionarSolicitudesProveedorUseCase;

    @InjectMocks private RegistroProveedorListener listener;

    @ParameterizedTest(name = "rol = {0}")
    @ValueSource(strings = {"ROLE_PROVEEDOR", "PROVEEDOR"})
    @DisplayName("Al registrarse un proveedor genera su solicitud de aprobación")
    void createsRequestForProvider(String rol) {
        listener.onUsuarioRegistrado(new UsuarioRegistradoEvent(42L, rol));

        verify(gestionarSolicitudesProveedorUseCase).registrarSolicitud(42L);
    }

    @ParameterizedTest(name = "rol = {0}")
    @ValueSource(strings = {"ROLE_CLIENTE", "ROLE_ADMIN"})
    @DisplayName("Ignora el registro de usuarios que no son proveedores")
    void ignoresOtherRoles(String rol) {
        listener.onUsuarioRegistrado(new UsuarioRegistradoEvent(42L, rol));

        verifyNoInteractions(gestionarSolicitudesProveedorUseCase);
    }
}
