package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ServicioTest {

    @Test
    @DisplayName("Debe crear un servicio válido exitosamente")
    void debeCrearServicioValido() {
        Servicio servicio = Servicio.crearNuevo(1, "Corte de Cabello", 30);

        assertNotNull(servicio);
        assertNull(servicio.getId());
        assertEquals(1, servicio.getIdProveedor());
        assertEquals("Corte de Cabello", servicio.getNombre());
        assertEquals(30, servicio.getDuracionMinutos());
        assertNull(servicio.getCreatedAt());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -30})
    @DisplayName("Debe fallar al crear un servicio con duración menor o igual a 0")
    void debeFallarConDuracionInvalida(int duracionInvalida) {
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(1, "Manicura", duracionInvalida)
        );

        assertTrue(ex.getMessage().contains("duración del servicio debe ser mayor a 0 minutos"));
    }

    @Test
    @DisplayName("Debe fallar al crear un servicio con duración nula")
    void debeFallarConDuracionNula() {
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(1, "Manicura", null)
        );

        assertTrue(ex.getMessage().contains("duración del servicio debe ser mayor a 0 minutos"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n"})
    @DisplayName("Debe fallar al crear un servicio con nombre vacío o en blanco")
    void debeFallarConNombreVacio(String nombreInvalido) {
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(1, nombreInvalido, 45)
        );

        assertTrue(ex.getMessage().contains("nombre del servicio no puede estar vacío"));
    }

    @Test
    @DisplayName("Debe fallar al crear un servicio con nombre nulo")
    void debeFallarConNombreNulo() {
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(1, null, 45)
        );

        assertTrue(ex.getMessage().contains("nombre del servicio no puede estar vacío"));
    }

    @Test
    @DisplayName("Debe fallar al crear un servicio con proveedor nulo")
    void debeFallarConProveedorNulo() {
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(null, "Masaje Relajante", 60)
        );

        assertTrue(ex.getMessage().contains("id del proveedor es obligatorio"));
    }
}
