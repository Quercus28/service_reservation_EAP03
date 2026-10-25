package com.service_reservation.EAP03.modulos.admin.domain.model;

import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudAprobacionProveedorTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 10, 0);

    @Test
    @DisplayName("Una solicitud nueva queda pendiente y sin resolver")
    void newRequestIsPending() {
        SolicitudAprobacionProveedor solicitud = SolicitudAprobacionProveedor.nueva(42L, AHORA);

        assertNull(solicitud.getId());
        assertEquals(42L, solicitud.getIdUsuario());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertEquals(AHORA, solicitud.getFechaSolicitud());
        assertNull(solicitud.getFechaResolucion());
        assertNull(solicitud.getIdAdminResolutor());
        assertNull(solicitud.getMotivoRechazo());
        assertTrue(solicitud.estaPendiente());
    }

    @Test
    @DisplayName("Rechaza una solicitud sin usuario")
    void rejectsMissingUser() {
        assertThrows(DatosAdminInvalidosException.class, () -> SolicitudAprobacionProveedor.nueva(null, AHORA));
    }

    @Test
    @DisplayName("Aprobar registra el administrador y la fecha")
    void approves() {
        // Arrange
        SolicitudAprobacionProveedor solicitud = SolicitudAprobacionProveedor.nueva(42L, AHORA);

        // Act
        solicitud.aprobar(1L, AHORA.plusHours(1));

        // Assert
        assertEquals(EstadoSolicitud.APROBADA, solicitud.getEstado());
        assertEquals(1L, solicitud.getIdAdminResolutor());
        assertEquals(AHORA.plusHours(1), solicitud.getFechaResolucion());
        assertFalse(solicitud.estaPendiente());
    }

    @Test
    @DisplayName("Rechazar guarda el motivo recortado")
    void rejectsWithTrimmedReason() {
        // Arrange
        SolicitudAprobacionProveedor solicitud = SolicitudAprobacionProveedor.nueva(42L, AHORA);

        // Act
        solicitud.rechazar(1L, "  Documentos incompletos  ", AHORA.plusHours(1));

        // Assert
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertEquals("Documentos incompletos", solicitud.getMotivoRechazo());
        assertEquals(1L, solicitud.getIdAdminResolutor());
    }

    @Test
    @DisplayName("No permite resolver una solicitud ya resuelta")
    void rejectsResolvingTwice() {
        // Arrange
        SolicitudAprobacionProveedor solicitud = new SolicitudAprobacionProveedor(5L, 42L, EstadoSolicitud.APROBADA,
                AHORA, AHORA, 1L, null);

        // Act
        OperacionAdminInvalidaException ex = assertThrows(OperacionAdminInvalidaException.class,
                () -> solicitud.aprobar(1L, AHORA));

        // Assert
        assertEquals("La solicitud 5 ya fue resuelta con estado APROBADA", ex.getMessage());
        assertThrows(OperacionAdminInvalidaException.class, () -> solicitud.rechazar(1L, "Motivo", AHORA));
        assertEquals(5L, solicitud.getId());
    }

    @ParameterizedTest(name = "motivo = [{0}]")
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("El motivo es obligatorio")
    void reasonIsRequired(String motivo) {
        DatosAdminInvalidosException ex = assertThrows(DatosAdminInvalidosException.class,
                () -> SolicitudAprobacionProveedor.validarMotivo(motivo));

        assertEquals("El motivo es obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("Acepta un motivo de 500 caracteres y rechaza uno más largo")
    void reasonLengthLimit() {
        assertEquals(500, SolicitudAprobacionProveedor.validarMotivo("a".repeat(500)).length());

        DatosAdminInvalidosException ex = assertThrows(DatosAdminInvalidosException.class,
                () -> SolicitudAprobacionProveedor.validarMotivo("a".repeat(501)));
        assertEquals("El motivo no puede superar 500 caracteres", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza un motivo con caracteres no permitidos")
    void rejectsUnsafeReason() {
        DatosAdminInvalidosException ex = assertThrows(DatosAdminInvalidosException.class,
                () -> SolicitudAprobacionProveedor.validarMotivo("<script>alert(1)</script>"));

        assertEquals("El motivo contiene caracteres no permitidos o secuencias sospechosas", ex.getMessage());
    }

    @Test
    @DisplayName("Acepta un motivo en español con tildes y signos")
    void acceptsSpanishReason() {
        assertEquals("¿Documentación vencida? Sí, revisar: NIT y teléfono.",
                SolicitudAprobacionProveedor.validarMotivo("¿Documentación vencida? Sí, revisar: NIT y teléfono."));
    }
}
