package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.Motivo;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultaHistorialParametrosTest {

    private static Motivo motivoDe(Map<String, String[]> params) {
        return assertThrows(ConsultaHistorialInvalidaException.class, () -> ConsultaHistorialParametros.desde(params)).getMotivo();
    }

    @Test
    @DisplayName("Sin parámetros usa los valores por defecto")
    void defaults() {
        ConsultaHistorialParametros p = ConsultaHistorialParametros.desde(Map.of());
        assertNull(p.idCliente());
        assertNull(p.estado());
        assertEquals(0, p.page());
        assertEquals(10, p.size());
    }

    @Test
    @DisplayName("Acepta estado, página, tamaño e idCliente válidos")
    void validParams() {
        ConsultaHistorialParametros p = ConsultaHistorialParametros.desde(Map.of(
                "estado", new String[]{"CANCELADO"}, "page", new String[]{"2"},
                "size", new String[]{"50"}, "idCliente", new String[]{"3"}));
        assertEquals(EstadoReserva.CANCELADO, p.estado());
        assertEquals(2, p.page());
        assertEquals(50, p.size());
        assertEquals(3L, p.idCliente());
    }

    @Test
    @DisplayName("Rechaza texto con aspecto de comando")
    void rejectsSuspiciousText() {
        assertEquals(Motivo.PATRON_SOSPECHOSO, motivoDe(Map.of("estado", new String[]{"' OR 1=1 --"})));
        assertEquals(Motivo.PATRON_SOSPECHOSO, motivoDe(Map.of("idCliente", new String[]{"1;DROP TABLE reservas"})));
        assertEquals(Motivo.PATRON_SOSPECHOSO, motivoDe(Map.of("x<script>", new String[]{"1"})));
    }

    @Test
    @DisplayName("Rechaza parámetros desconocidos, valores fuera de formato o de rango y parámetros repetidos")
    void rejectsInvalidParams() {
        assertEquals(Motivo.PARAMETRO_DESCONOCIDO, motivoDe(Map.of("orden", new String[]{"asc"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("estado", new String[]{"INEXISTENTE"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("size", new String[]{"51"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("size", new String[]{"0"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("page", new String[]{"-1"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("idCliente", new String[]{"abc"})));
        assertEquals(Motivo.VALOR_INVALIDO, motivoDe(Map.of("page", new String[]{"1", "2"})));
    }
}