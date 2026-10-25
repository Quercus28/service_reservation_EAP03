package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.AsociacionNoEncontradaException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoEnConflictoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDeOtroProveedorException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDuplicadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoOperativoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoYaAsociadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.ServicioNoExisteException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecursoExceptionHandlerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 8, 0);

    private final RecursoExceptionHandler handler = new RecursoExceptionHandler();

    private static void assertError(ResponseEntity<Map<String, Object>> response, int status, String errorCode, RuntimeException ex) {
        assertEquals(status, response.getStatusCode().value());
        assertEquals(List.of("errorCode", "message", "traceId"), List.copyOf(response.getBody().keySet()));
        assertEquals(errorCode, response.getBody().get("errorCode"));
        assertEquals(ex.getMessage(), response.getBody().get("message"));
        assertNotNull(response.getBody().get("traceId"));
    }

    @Test
    @DisplayName("Los recursos, servicios, asociaciones, horarios y bloqueos inexistentes responden 404")
    void notFoundErrorsReturn404() {
        ServicioNoExisteException servicio = new ServicioNoExisteException(10);
        AsociacionNoEncontradaException asociacion = new AsociacionNoEncontradaException(10, 3);
        RecursoNoEncontradoException recurso = new RecursoNoEncontradoException(3);
        HorarioNoEncontradoException horario = new HorarioNoEncontradoException(3, 5);
        BloqueoNoEncontradoException bloqueo = new BloqueoNoEncontradoException(3, 8);

        assertError(handler.manejarServicioNoExiste(servicio), 404, "SERVICIO_NO_ENCONTRADO", servicio);
        assertError(handler.manejarAsociacionNoEncontrada(asociacion), 404, "RECURSO_NO_ASOCIADO", asociacion);
        assertError(handler.manejarNoEncontrado(recurso), 404, "RECURSO_NO_ENCONTRADO", recurso);
        assertError(handler.manejarHorarioNoEncontrado(horario), 404, "HORARIO_NO_ENCONTRADO", horario);
        assertError(handler.manejarBloqueoNoEncontrado(bloqueo), 404, "BLOQUEO_NO_ENCONTRADO", bloqueo);
    }

    @Test
    @DisplayName("Los conflictos de asociación, propiedad y estado del recurso responden 409")
    void resourceConflictsReturn409() {
        RecursoYaAsociadoException yaAsociado = new RecursoYaAsociadoException(3, 10);
        RecursoDeOtroProveedorException otroProveedor = new RecursoDeOtroProveedorException(3, 10);
        RecursoDesactivadoException desactivado = new RecursoDesactivadoException(3);
        RecursoDuplicadoException duplicado = new RecursoDuplicadoException("Camilla");

        assertError(handler.manejarYaAsociado(yaAsociado), 409, "RECURSO_YA_ASOCIADO", yaAsociado);
        assertError(handler.manejarOtroProveedor(otroProveedor), 409, "RECURSO_DE_OTRO_PROVEEDOR", otroProveedor);
        assertError(handler.manejarDesactivado(desactivado), 409, "RECURSO_DESACTIVADO", desactivado);
        assertError(handler.manejarDuplicado(duplicado), 409, "RECURSO_DUPLICADO", duplicado);
    }

    @Test
    @DisplayName("Los conflictos de disponibilidad, horarios y bloqueos responden 409")
    void availabilityConflictsReturn409() {
        DisponibilidadInsuficienteException disponibilidad = new DisponibilidadInsuficienteException(3, 2, 1);
        HorarioSolapadoException solapado = new HorarioSolapadoException(
                HorarioRecurso.reconstruir(5, 3, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0)));
        BloqueoEnConflictoException conflicto = BloqueoEnConflictoException.conBloqueo(
                BloqueoRecurso.reconstruir(8, 3, INICIO, INICIO.plusHours(1), null, INICIO));
        RecursoNoOperativoException noOperativo = new RecursoNoOperativoException(3, INICIO, INICIO.plusHours(1));

        assertError(handler.manejarDisponibilidad(disponibilidad), 409, "DISPONIBILIDAD_INSUFICIENTE", disponibilidad);
        assertError(handler.manejarHorarioSolapado(solapado), 409, "HORARIO_SOLAPADO", solapado);
        assertError(handler.manejarBloqueoEnConflicto(conflicto), 409, "BLOQUEO_EN_CONFLICTO", conflicto);
        assertError(handler.manejarNoOperativo(noOperativo), 409, "RECURSO_NO_OPERATIVO", noOperativo);
    }

    @Test
    @DisplayName("Los datos inválidos responden 400")
    void invalidDataReturns400() {
        DatosInvalidosException ex = new DatosInvalidosException("El stock no puede ser negativo");

        assertError(handler.manejarDatosInvalidos(ex), 400, "DATOS_INVALIDOS", ex);
    }
}
