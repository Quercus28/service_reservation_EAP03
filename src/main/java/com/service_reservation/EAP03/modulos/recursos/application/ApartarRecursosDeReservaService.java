package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoDesactivadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoDisponibleParaReservaException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoOperativoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoOpcionalNoValidoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ApartarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApartarRecursosDeReservaService implements ApartarRecursosDeReservaUseCase {

    private final ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;
    private final RecursoRepositoryPort recursoRepositoryPort;
    private final RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;
    private final ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;

    public ApartarRecursosDeReservaService(ServicioRecursoRepositoryPort servicioRecursoRepositoryPort,
                                           RecursoRepositoryPort recursoRepositoryPort,
                                           RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase,
                                           ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase) {
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.registrarPrestamoRecursoUseCase = registrarPrestamoRecursoUseCase;
        this.obtenerServicioPorIdUseCase = obtenerServicioPorIdUseCase;
    }

    // Si algún recurso falla se lanza el error. Como los préstamos van en la misma transacción que la
    // reserva, no queda guardado ningún préstamo ni la reserva.
    @Override
    @Transactional
    public List<RecursoPrestado> ejecutar(Integer idReserva, Integer idServicio, LocalDateTime inicio,
                                          LocalDateTime fin, Collection<Integer> idsOpcionalesElegidos) {
        Set<Integer> elegidos = idsOpcionalesElegidos == null ? Set.of() : Set.copyOf(idsOpcionalesElegidos);
        List<ServicioRecurso> asociados = servicioRecursoRepositoryPort.buscarPorServicio(idServicio);

        // 1. El cliente solo puede pedir recursos que el servicio ofrece como adicionales.
        Set<Integer> opcionalesDelServicio = asociados.stream()
                .map(ServicioRecurso::getIdRecurso)
                .collect(Collectors.toSet());
        List<Integer> noValidos = elegidos.stream()
                .filter(id -> !opcionalesDelServicio.contains(id))
                .sorted()
                .toList();
        if (!noValidos.isEmpty()) {
            throw new RecursoOpcionalNoValidoException(nombreDeServicioParaMensaje(idServicio), noValidos.stream()
                    .map(this::nombreParaMensaje)
                    .toList());
        }

        // 2. Se apartan solo los que pidió el cliente; si no pidió nada, no se aparta nada.
        //    Van siempre en orden de id del recurso. Así dos reservas que piden los mismos
        //    recursos los toman en el mismo orden y ninguna se queda esperando a la otra para
        //    siempre.
        List<ServicioRecurso> aApartar = asociados.stream()
                .filter(a -> elegidos.contains(a.getIdRecurso()))
                .sorted(Comparator.comparing(ServicioRecurso::getIdRecurso))
                .toList();

        List<RecursoPrestado> prestamos = new ArrayList<>();
        for (ServicioRecurso asociacion : aApartar) {
            prestamos.add(apartar(asociacion, idReserva, inicio, fin));
        }
        return prestamos;
    }

    // El nombre del recurso entre comillas; si no existe, su número.
    private String nombreParaMensaje(Integer idRecurso) {
        return recursoRepositoryPort.buscarPorId(idRecurso)
                .map(r -> "'" + r.getNombre() + "'")
                .orElse(String.valueOf(idRecurso));
    }

    // El nombre del servicio entre comillas, preguntándole al catálogo; si no está, su número.
    private String nombreDeServicioParaMensaje(Integer idServicio) {
        try {
            return "'" + obtenerServicioPorIdUseCase.ejecutar(idServicio).nombre() + "'";
        } catch (ServicioNoEncontradoException e) {
            return String.valueOf(idServicio);
        }
    }

    private RecursoPrestado apartar(ServicioRecurso asociacion, Integer idReserva,
                                    LocalDateTime inicio, LocalDateTime fin) {
        Integer idRecurso = asociacion.getIdRecurso();
        int cantidad = asociacion.getCantidad();

        // Se toma el recurso con bloqueo de la fila, igual que al registrar un préstamo.
        Recurso recurso = recursoRepositoryPort.buscarPorIdConBloqueo(idRecurso)
                .orElseThrow(() -> new RecursoNoEncontradoException(idRecurso));

        // Los adicionales se cobran aparte: precio unitario por la cantidad de la asociación.
        BigDecimal precioTotal = recurso.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));

        // Desactivado, stock, horario y bloqueos se revisan con la misma lógica de un préstamo
        // normal. Los mensajes se cambian para que hablen de la reserva.
        try {
            return registrarPrestamoRecursoUseCase.ejecutar(new RegistrarPrestamoComando(
                    idRecurso, idReserva, cantidad, inicio, fin, precioTotal));
        } catch (RecursoDesactivadoException e) {
            throw RecursoDesactivadoException.alReservar(recurso.getNombre());
        } catch (DisponibilidadInsuficienteException e) {
            throw RecursoNoDisponibleParaReservaException.sinUnidades(recurso.getNombre(), cantidad, e.getDisponible());
        } catch (RecursoNoOperativoException e) {
            throw RecursoNoDisponibleParaReservaException.noOperativo(recurso.getNombre());
        }
    }
}
