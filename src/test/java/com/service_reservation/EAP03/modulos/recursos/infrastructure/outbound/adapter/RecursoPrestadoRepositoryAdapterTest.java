package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.RecursoPrestadoMapper;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoPrestadoJpaEntity;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.repository.RecursoPrestadoSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoPrestadoRepositoryAdapterTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime FIN = INICIO.plusHours(2);
    private static final LocalDateTime LIBERADO = INICIO.minusDays(1);

    @Mock private RecursoPrestadoSpringDataRepository repositorio;

    private RecursoPrestadoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RecursoPrestadoRepositoryAdapter(repositorio, new RecursoPrestadoMapper());
    }

    private static RecursoPrestadoJpaEntity entity(Integer id) {
        RecursoPrestadoJpaEntity entidad = new RecursoPrestadoJpaEntity();
        entidad.setId(id);
        entidad.setIdRecurso(1);
        entidad.setIdServicioPrestado(100);
        entidad.setCantidad(2);
        entidad.setFechaInicio(INICIO);
        entidad.setFechaFin(FIN);
        entidad.setPrecioTotal(BigDecimal.TEN);
        entidad.setLiberadoEn(LIBERADO);
        return entidad;
    }

    @Test
    @DisplayName("Guardar convierte el préstamo a entidad y devuelve el préstamo guardado")
    void savesLoan() {
        // Arrange
        when(repositorio.save(any())).thenReturn(entity(4));

        // Act
        RecursoPrestado guardado = adapter.guardar(RecursoPrestado.reconstruir(null, 1, 100, 2, INICIO, FIN, BigDecimal.TEN, LIBERADO));

        // Assert
        ArgumentCaptor<RecursoPrestadoJpaEntity> captor = ArgumentCaptor.forClass(RecursoPrestadoJpaEntity.class);
        verify(repositorio).save(captor.capture());
        RecursoPrestadoJpaEntity enviada = captor.getValue();
        assertNull(enviada.getId());
        assertEquals(1, enviada.getIdRecurso());
        assertEquals(100, enviada.getIdServicioPrestado());
        assertEquals(2, enviada.getCantidad());
        assertEquals(INICIO, enviada.getFechaInicio());
        assertEquals(FIN, enviada.getFechaFin());
        assertEquals(BigDecimal.TEN, enviada.getPrecioTotal());
        assertEquals(LIBERADO, enviada.getLiberadoEn());
        assertEquals(4, guardado.getId());
        assertTrue(guardado.estaLiberado());
    }

    @Test
    @DisplayName("Busca los préstamos que se solapan con un rango")
    void findsOverlappingLoans() {
        when(repositorio.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(entity(4)));

        assertEquals(List.of(4), adapter.buscarQueSeSolapan(1, INICIO, FIN).stream().map(RecursoPrestado::getId).toList());
    }

    @Test
    @DisplayName("Busca los préstamos activos de una reserva")
    void findsActiveLoansOfReservation() {
        when(repositorio.findByIdServicioPrestadoAndLiberadoEnIsNull(100)).thenReturn(List.of(entity(4), entity(5)));

        assertEquals(List.of(4, 5), adapter.buscarActivosDeReserva(100).stream().map(RecursoPrestado::getId).toList());
    }
}
