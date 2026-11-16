package com.service_reservation.EAP03.modulos.admin.application;

import com.service_reservation.EAP03.modulos.admin.domain.model.AccionAdmin;
import com.service_reservation.EAP03.modulos.admin.domain.model.EventoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.RegistroAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.model.ResultadoAuditoria;
import com.service_reservation.EAP03.modulos.admin.domain.ports.out.AuditoriaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegistrarAuditoriaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 16, 9, 30);

    @Mock private AuditoriaPort auditoriaPort;

    private RegistrarAuditoriaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new RegistrarAuditoriaService(auditoriaPort, clock);
    }

    private RegistroAuditoria guardado() {
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaPort).registrar(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Guarda el intento con quién lo hizo, a quién afecta, la acción, el resultado, la ruta y el motivo")
    void guardaElIntento() {
        service.ejecutar(new EventoAuditoria(7L, 12L, AccionAdmin.HISTORIAL_AJENO_DENEGADO,
                ResultadoAuditoria.DENEGADO, "GET /api/v1/reservas/mias", "motivo=historial de otro cliente"));

        assertEquals(new RegistroAuditoria(AHORA, 7L, 12L, AccionAdmin.HISTORIAL_AJENO_DENEGADO, null,
                ResultadoAuditoria.DENEGADO, "GET /api/v1/reservas/mias", "motivo=historial de otro cliente"),
                guardado());
    }

    @Test
    @DisplayName("Acepta que no se sepa quién hizo la petición ni a quién afecta")
    void aceptaIdsVacios() {
        service.ejecutar(new EventoAuditoria(null, null, AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO,
                ResultadoAuditoria.DENEGADO, null, "  "));

        RegistroAuditoria registro = guardado();
        assertNull(registro.idActor());
        assertNull(registro.idUsuarioAfectado());
        assertNull(registro.endpoint());
        assertNull(registro.detalle());
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, registro.accion());
    }

    @Test
    @DisplayName("El detalle no guarda datos personales: correos y teléfonos se reemplazan")
    void detalleSinDatosPersonales() {
        service.ejecutar(new EventoAuditoria(7L, 12L, AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO,
                ResultadoAuditoria.DENEGADO, "PATCH /api/v1/reservas/5/estado",
                "motivo=reserva ajena correo=ana.perez@correo.com tel=+57 300 123 4567 cel=3001234567"));

        String detalle = guardado().detalle();
        assertFalse(detalle.contains("@"));
        assertFalse(detalle.contains("ana.perez"));
        assertFalse(detalle.contains("300 123 4567"));
        assertFalse(detalle.contains("3001234567"));
        assertEquals("motivo=reserva ajena correo=[omitido] tel=[omitido] cel=[omitido]", detalle);
    }

    @Test
    @DisplayName("La ruta se guarda sin los parámetros que mandó el usuario ni datos personales")
    void endpointSinParametros() {
        service.ejecutar(new EventoAuditoria(7L, null, AccionAdmin.HISTORIAL_AJENO_DENEGADO,
                ResultadoAuditoria.DENEGADO, "GET /api/v1/reservas/mias?idCliente=9&correo=a@b.co", "motivo=x"));

        assertEquals("GET /api/v1/reservas/mias", guardado().endpoint());
    }

    @Test
    @DisplayName("Los números cortos, como un id de reserva, se conservan")
    void conservaNumerosCortos() {
        service.ejecutar(new EventoAuditoria(7L, null, AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO,
                ResultadoAuditoria.DENEGADO, "PATCH /api/v1/reservas/123456/estado", "reserva=123456"));

        RegistroAuditoria registro = guardado();
        assertEquals("PATCH /api/v1/reservas/123456/estado", registro.endpoint());
        assertEquals("reserva=123456", registro.detalle());
    }

    @Test
    @DisplayName("Un detalle muy largo se recorta al máximo permitido")
    void recortaDetalleLargo() {
        service.ejecutar(new EventoAuditoria(7L, null, AccionAdmin.HISTORIAL_AJENO_DENEGADO,
                ResultadoAuditoria.DENEGADO, "GET /x", "a".repeat(400)));

        assertEquals(RegistrarAuditoriaService.DETALLE_MAX, guardado().detalle().length());
    }

    @Test
    @DisplayName("La acción y el resultado son obligatorios; sin ellos no se guarda nada")
    void accionYResultadoObligatorios() {
        EventoAuditoria sinAccion = new EventoAuditoria(7L, null, null, ResultadoAuditoria.DENEGADO, "GET /x", "motivo");
        EventoAuditoria sinResultado = new EventoAuditoria(7L, null, AccionAdmin.HISTORIAL_AJENO_DENEGADO, null, "GET /x", "motivo");

        assertThrows(NullPointerException.class, () -> service.ejecutar(null));
        assertThrows(NullPointerException.class, () -> service.ejecutar(sinAccion));
        assertThrows(NullPointerException.class, () -> service.ejecutar(sinResultado));
        verifyNoInteractions(auditoriaPort);
    }

    @Test
    @DisplayName("Un texto enorme no hace caer la búsqueda de datos personales y el correo igual se tapa")
    void textoEnormeNoSeCae() {
        String enorme = "correo=" + "a".repeat(1000) + "@b" + ".c".repeat(100_000) + " tel=" + "1 ".repeat(100_000);
        EventoAuditoria evento = new EventoAuditoria(7L, null, AccionAdmin.HISTORIAL_AJENO_DENEGADO,
                ResultadoAuditoria.DENEGADO, "GET /x", enorme);

        service.ejecutar(evento);

        assertEquals("correo=[omitido] tel=[omitido]", guardado().detalle());
    }

    @Test
    @DisplayName("Las fechas con guiones en el detalle también se tapan (el detalle debe ser texto fijo)")
    void tapaFechasConGuiones() {
        service.ejecutar(new EventoAuditoria(7L, null, AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO,
                ResultadoAuditoria.DENEGADO, "GET /x", "fecha=2026-11-16"));

        assertEquals("fecha=[omitido]", guardado().detalle());
    }

    @Test
    @DisplayName("Se guarda en su propia transacción, para que quede aunque quien llama deshaga la suya")
    void transaccionPropia() throws NoSuchMethodException {
        Transactional tx = RegistrarAuditoriaService.class
                .getMethod("ejecutar", EventoAuditoria.class)
                .getAnnotation(Transactional.class);

        assertNotNull(tx);
        assertEquals(Propagation.REQUIRES_NEW, tx.propagation());
    }

    @Test
    @DisplayName("Existen las acciones nuevas para otros módulos")
    void accionesNuevas() {
        assertEquals(AccionAdmin.HISTORIAL_AJENO_DENEGADO, AccionAdmin.valueOf("HISTORIAL_AJENO_DENEGADO"));
        assertEquals(AccionAdmin.CAMBIO_ESTADO_RESERVA_DENEGADO, AccionAdmin.valueOf("CAMBIO_ESTADO_RESERVA_DENEGADO"));
    }
}
