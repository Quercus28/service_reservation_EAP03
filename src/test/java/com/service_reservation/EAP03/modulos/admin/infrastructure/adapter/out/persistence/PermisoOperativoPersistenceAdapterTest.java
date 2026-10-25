package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.RolOperativo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermisoOperativoPersistenceAdapterTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks private PermisoOperativoPersistenceAdapter adapter;

    @Test
    @DisplayName("Devuelve el estado guardado del permiso del rol")
    void returnsStoredPermissionState() {
        when(jdbcTemplate.queryForList(anyString(), eq(Boolean.class), eq(42L), eq("ROLE_PROVEEDOR"))).thenReturn(List.of(true));

        assertEquals(Optional.of(true), adapter.estadoPermiso(42L, RolOperativo.PROVEEDOR));
    }

    @Test
    @DisplayName("Devuelve vacío si el usuario no tiene el rol")
    void returnsEmptyWithoutRole() {
        when(jdbcTemplate.queryForList(anyString(), eq(Boolean.class), eq(42L), eq("ROLE_CLIENTE"))).thenReturn(List.of());

        assertTrue(adapter.estadoPermiso(42L, RolOperativo.CLIENTE).isEmpty());
    }

    @Test
    @DisplayName("Actualiza el estado del permiso del rol")
    void updatesPermissionState() {
        adapter.actualizarEstado(42L, RolOperativo.CLIENTE, false);

        verify(jdbcTemplate).update(anyString(), eq(false), eq(42L), eq("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Lista los permisos de cada rol operativo del usuario")
    void listsPermissions() throws Exception {
        // Arrange
        ResultSet cliente = mock(ResultSet.class);
        when(cliente.getString("nombre")).thenReturn("ROLE_CLIENTE");
        when(cliente.getBoolean("activo")).thenReturn(true);
        ResultSet proveedor = mock(ResultSet.class);
        when(proveedor.getString("nombre")).thenReturn("ROLE_PROVEEDOR");
        when(proveedor.getBoolean("activo")).thenReturn(false);
        doAnswer(inv -> {
            RowCallbackHandler handler = inv.getArgument(1);
            handler.processRow(cliente);
            handler.processRow(proveedor);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class), eq(42L));

        // Act
        Map<RolOperativo, Boolean> permisos = adapter.listarPermisos(42L);

        // Assert
        assertEquals(Map.of(RolOperativo.CLIENTE, true, RolOperativo.PROVEEDOR, false), permisos);
    }
}
