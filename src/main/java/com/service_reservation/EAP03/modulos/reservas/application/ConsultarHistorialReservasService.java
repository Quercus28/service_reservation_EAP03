package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaReservas;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ReservaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ConsultarHistorialReservasUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarHistorialReservasService implements ConsultarHistorialReservasUseCase {

    private final ConsultarClienteUseCase consultarClienteUseCase;
    private final ReservaRepositoryPort reservaRepositoryPort;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;

    public ConsultarHistorialReservasService(ConsultarClienteUseCase consultarClienteUseCase,
                                             ReservaRepositoryPort reservaRepositoryPort,
                                             ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase) {
        this.consultarClienteUseCase = consultarClienteUseCase;
        this.reservaRepositoryPort = reservaRepositoryPort;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaHistorial ejecutar(ConsultarHistorialComando comando) {
        // El cliente se resuelve siempre desde el usuario autenticado, nunca desde la solicitud.
        Long idCliente = comando.idUsuario() == null ? null
                : consultarClienteUseCase.buscarIdClientePorUsuario(comando.idUsuario()).orElse(null);
        if (idCliente == null) {
            throw new UsuarioNoEsClienteException();
        }
        // Si la solicitud nombra a otro cliente, se corta antes de tocar las reservas.
        if (comando.idClienteSolicitado() != null && !comando.idClienteSolicitado().equals(idCliente)) {
            throw new HistorialAjenoException();
        }

        PaginaReservas pagina = reservaRepositoryPort.buscarHistorialDeCliente(
                idCliente, comando.estado(), comando.page(), comando.size());

        // Un servicio puede repetirse en varias reservas: se busca su nombre una sola vez.
        Map<Integer, String> nombres = new HashMap<>();
        List<ReservaHistorial> items = pagina.contenido().stream()
                .map(r -> new ReservaHistorial(r, nombreDe(r, nombres)))
                .toList();
        return new PaginaHistorial(items, pagina.page(), pagina.size(), pagina.total());
    }

    // Si el servicio ya no existe, la cita se conserva en el historial sin nombre.
    private String nombreDe(Reserva reserva, Map<Integer, String> cache) {
        return cache.computeIfAbsent(reserva.getIdServicio(), id -> {
            try {
                return obtenerServicioPorIdUseCase.ejecutar(id).nombre();
            } catch (ServicioNoEncontradoException e) {
                return null;
            }
        });
    }
}