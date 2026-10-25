package com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.reservas.domain.model.Cancelacion;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.ReservaMapper;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.entity.ReservaJpaEntity;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.repository.ReservaSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaRepositoryAdapterTest {

    private static final List<EstadoReserva> ACTIVOS = List.of(EstadoReserva.RESERVADO, EstadoReserva.EN_PROCESO);
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(1);
    private static final LocalDateTime CREADA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final LocalDateTime CANCELADA = CREADA.plusHours(1);

    @Mock private ReservaSpringDataRepository repositorio;

    private ReservaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ReservaRepositoryAdapter(repositorio, new ReservaMapper());
    }

    private static ReservaJpaEntity entity(Integer id) {
        ReservaJpaEntity entidad = new ReservaJpaEntity();
        entidad.setId(id);
        entidad.setIdCliente(3L);
        entidad.setIdServicio(10);
        entidad.setIdProveedor(7);
        entidad.setFechaInicio(INICIO);
        entidad.setFechaFin(FIN);
        entidad.setEstado(EstadoReserva.CANCELADO);
        entidad.setNotas("Nota");
        entidad.setCreatedAt(CREADA);
        entidad.setCanceladaEn(CANCELADA);
        entidad.setMotivoCancelacion("Viaje");
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte la reserva a entidad y devuelve la reserva guardada")
    void savesReservation() {
        // Arrange
        when(repositorio.save(any())).thenReturn(entity(50));
        Reserva reserva = Reserva.reconstruir(null, new PartesReserva(3L, 10, 7), new IntervaloReservado(INICIO, FIN),
                EstadoReserva.CANCELADO, "Nota", CREADA, new Cancelacion(CANCELADA, "Viaje"));

        // Act
        Reserva guardada = adapter.guardar(reserva);

        // Assert
        ArgumentCaptor<ReservaJpaEntity> captor = ArgumentCaptor.forClass(ReservaJpaEntity.class);
        verify(repositorio).save(captor.capture());
        ReservaJpaEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(3L, enviada.getIdCliente());
        assertEquals(10, enviada.getIdServicio());
        assertEquals(7, enviada.getIdProveedor());
        assertEquals(INICIO, enviada.getFechaInicio());
        assertEquals(FIN, enviada.getFechaFin());
        assertEquals(EstadoReserva.CANCELADO, enviada.getEstado());
        assertEquals("Nota", enviada.getNotas());
        assertEquals(CREADA, enviada.getCreatedAt());
        assertEquals(CANCELADA, enviada.getCanceladaEn());
        assertEquals("Viaje", enviada.getMotivoCancelacion());
        assertEquals(50, guardada.getId());
        assertEquals("Viaje", guardada.getMotivoCancelacion());
        assertEquals(CANCELADA, guardada.getCanceladaEn());
    }

    @Test
    @DisplayName("Busca una reserva por id")
    void findsById() {
        when(repositorio.findById(50)).thenReturn(Optional.of(entity(50)));
        when(repositorio.findById(51)).thenReturn(Optional.empty());

        assertEquals(10, adapter.buscarPorId(50).orElseThrow().getIdServicio());
        assertTrue(adapter.buscarPorId(51).isEmpty());
    }

    @Test
    @DisplayName("Busca solo reservas en estados activos que se solapan con el rango")
    void findsActiveOverlappingReservations() {
        when(repositorio.buscarQueSeSolapan(10, ACTIVOS, INICIO, FIN)).thenReturn(List.of(entity(50)));

        assertEquals(List.of(50), adapter.buscarActivasQueSeSolapan(10, INICIO, FIN).stream().map(Reserva::getId).toList());
    }

    @Test
    @DisplayName("Busca las reservas activas de un cliente y de un proveedor")
    void findsActiveReservationsOfClientAndProvider() {
        when(repositorio.findByIdClienteAndEstadoIn(3L, ACTIVOS)).thenReturn(List.of(entity(50)));
        when(repositorio.findByIdProveedorAndEstadoIn(7, ACTIVOS)).thenReturn(List.of(entity(51), entity(52)));

        assertEquals(List.of(50), adapter.buscarActivasDeCliente(3L).stream().map(Reserva::getId).toList());
        assertEquals(List.of(51, 52), adapter.buscarActivasDeProveedor(7).stream().map(Reserva::getId).toList());
    }

    @Test
    @DisplayName("Cuenta las reservas activas de un cliente y de un proveedor")
    void countsActiveReservations() {
        when(repositorio.countByIdClienteAndEstadoIn(3L, ACTIVOS)).thenReturn(4L);
        when(repositorio.countByIdProveedorAndEstadoIn(7, ACTIVOS)).thenReturn(2L);

        assertEquals(4, adapter.contarActivasDeCliente(3L));
        assertEquals(2, adapter.contarActivasDeProveedor(7));
    }
}
