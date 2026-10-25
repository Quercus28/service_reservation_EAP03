package com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;

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

    @Test
    @DisplayName("Debe recortar los espacios del nombre al crear un servicio")
    void debeRecortarNombreAlCrear() {
        // Act
        Servicio servicio = Servicio.crearNuevo(1, "   Pedicura Spa  ", 50);

        // Assert
        assertEquals("Pedicura Spa", servicio.getNombre());
    }

    @Test
    @DisplayName("Debe aceptar un nombre de exactamente 150 caracteres")
    void debeAceptarNombreEnElLimite() {
        // Arrange
        String nombre = "a".repeat(150);

        // Act
        Servicio servicio = Servicio.crearNuevo(1, nombre, 30);

        // Assert
        assertEquals(150, servicio.getNombre().length());
    }

    @Test
    @DisplayName("Debe aceptar un nombre de 150 caracteres útiles rodeado de espacios")
    void debeAceptarNombreEnElLimiteConEspacios() {
        // Arrange
        String nombre = "  " + "b".repeat(150) + "  ";

        // Act
        Servicio servicio = Servicio.crearNuevo(1, nombre, 30);

        // Assert
        assertEquals("b".repeat(150), servicio.getNombre());
    }

    @Test
    @DisplayName("Debe fallar al crear un servicio con nombre de más de 150 caracteres")
    void debeFallarConNombreDemasiadoLargo() {
        // Arrange
        String nombre = "a".repeat(151);

        // Act
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.crearNuevo(1, nombre, 30)
        );

        // Assert
        assertTrue(ex.getMessage().contains("no puede exceder los 150 caracteres"));
    }

    @Test
    @DisplayName("Debe aceptar la duración mínima de 1 minuto")
    void debeAceptarDuracionMinima() {
        // Act
        Servicio servicio = Servicio.crearNuevo(1, "Consulta rápida", 1);

        // Assert
        assertEquals(1, servicio.getDuracionMinutos());
    }

    @Test
    @DisplayName("Debe reconstituir un servicio persistido con todos sus datos")
    void debeReconstituirServicio() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.parse("2026-10-01T08:30:00-05:00");

        // Act
        Servicio servicio = Servicio.reconstituir(7, 3, " Masaje ", 60, creado);

        // Assert
        assertEquals(7, servicio.getId());
        assertEquals(3, servicio.getIdProveedor());
        assertEquals("Masaje", servicio.getNombre());
        assertEquals(60, servicio.getDuracionMinutos());
        assertEquals(creado, servicio.getCreatedAt());
    }

    @Test
    @DisplayName("Debe fallar al reconstituir un servicio sin id")
    void debeFallarAlReconstituirSinId() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.now();

        // Act
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.reconstituir(null, 3, "Masaje", 60, creado)
        );

        // Assert
        assertTrue(ex.getMessage().contains("id del servicio es obligatorio"));
    }

    @Test
    @DisplayName("Debe validar las invariantes también al reconstituir")
    void debeValidarInvariantesAlReconstituir() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.now();

        // Act
        ServicioInvalidoException ex = assertThrows(
                ServicioInvalidoException.class,
                () -> Servicio.reconstituir(7, 3, "Masaje", 0, creado)
        );

        // Assert
        assertTrue(ex.getMessage().contains("duración del servicio debe ser mayor a 0 minutos"));
    }

    @Test
    @DisplayName("Debe actualizar nombre y duración conservando id, proveedor y fecha de creación")
    void debeActualizarServicio() {
        // Arrange
        OffsetDateTime creado = OffsetDateTime.now();
        Servicio servicio = Servicio.reconstituir(7, 3, "Masaje", 60, creado);

        // Act
        servicio.actualizar("  Masaje profundo ", 90);

        // Assert
        assertEquals("Masaje profundo", servicio.getNombre());
        assertEquals(90, servicio.getDuracionMinutos());
        assertEquals(7, servicio.getId());
        assertEquals(3, servicio.getIdProveedor());
        assertEquals(creado, servicio.getCreatedAt());
    }

    @Test
    @DisplayName("Una actualización inválida no debe modificar el servicio")
    void actualizacionInvalidaNoModificaServicio() {
        // Arrange
        Servicio servicio = Servicio.reconstituir(7, 3, "Masaje", 60, OffsetDateTime.now());

        // Act
        assertThrows(ServicioInvalidoException.class, () -> servicio.actualizar("  ", 90));
        assertThrows(ServicioInvalidoException.class, () -> servicio.actualizar("Masaje", -5));

        // Assert
        assertEquals("Masaje", servicio.getNombre());
        assertEquals(60, servicio.getDuracionMinutos());
    }

    @Test
    @DisplayName("Dos servicios con el mismo id son iguales y comparten hashCode")
    void serviciosConMismoIdSonIguales() {
        // Arrange
        Servicio a = Servicio.reconstituir(5, 1, "Corte", 30, OffsetDateTime.now());
        Servicio b = Servicio.reconstituir(5, 2, "Tinte", 90, null);

        // Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Servicios con distinto id, nulos u objetos de otro tipo no son iguales")
    void serviciosDistintosNoSonIguales() {
        // Arrange
        Servicio a = Servicio.reconstituir(5, 1, "Corte", 30, null);
        Servicio b = Servicio.reconstituir(6, 1, "Corte", 30, null);

        // Act
        boolean igualASiMismo = a.equals(a);
        boolean igualANulo = a.equals(null);
        boolean igualAOtroTipo = a.equals("Corte");

        // Assert
        assertTrue(igualASiMismo);
        assertNotEquals(a, b);
        assertFalse(igualANulo);
        assertFalse(igualAOtroTipo);
    }

    @Test
    @DisplayName("toString debe incluir los datos del servicio")
    void toStringIncluyeDatos() {
        // Arrange
        Servicio servicio = Servicio.reconstituir(5, 1, "Corte", 30, null);

        // Act
        String texto = servicio.toString();

        // Assert
        assertTrue(texto.contains("id=5"));
        assertTrue(texto.contains("idProveedor=1"));
        assertTrue(texto.contains("nombre='Corte'"));
        assertTrue(texto.contains("duracionMinutos=30"));
    }
}
