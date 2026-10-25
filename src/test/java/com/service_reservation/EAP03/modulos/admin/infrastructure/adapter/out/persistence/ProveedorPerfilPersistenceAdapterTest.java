package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.admin.domain.model.ProveedorPerfilDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorPerfilPersistenceAdapterTest {

    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks private ProveedorPerfilPersistenceAdapter adapter;

    @Test
    @DisplayName("Devuelve el perfil del proveedor leído de la base de datos")
    void returnsProfile() throws Exception {
        // Arrange
        ResultSet fila = mock(ResultSet.class);
        when(fila.getLong("id_usuario")).thenReturn(42L);
        when(fila.getString("razon_social")).thenReturn("Empresa SAS");
        when(fila.getString("nit_rut")).thenReturn("900123-1");
        when(fila.getString("telefono")).thenReturn("3001234567");
        when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ProveedorPerfilDTO>>any(), eq(42L)))
                .thenAnswer(inv -> inv.<RowMapper<ProveedorPerfilDTO>>getArgument(1).mapRow(fila, 0));

        // Act & Assert
        assertEquals(new ProveedorPerfilDTO(42L, "Empresa SAS", "900123-1", "3001234567"),
                adapter.buscarPorIdUsuario(42L).orElseThrow());
    }

    @Test
    @DisplayName("Devuelve vacío si el usuario no tiene perfil de proveedor")
    void returnsEmptyWithoutProfile() {
        when(jdbcTemplate.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ProveedorPerfilDTO>>any(), eq(42L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertTrue(adapter.buscarPorIdUsuario(42L).isEmpty());
    }

    @Test
    @DisplayName("Actualiza razón social, NIT y teléfono del proveedor")
    void updatesProfile() {
        adapter.actualizarPerfil(42L, "Empresa SAS", "900123-1", "3001234567");

        verify(jdbcTemplate).update(anyString(), eq("Empresa SAS"), eq("900123-1"), eq("3001234567"), eq(42L));
    }
}
