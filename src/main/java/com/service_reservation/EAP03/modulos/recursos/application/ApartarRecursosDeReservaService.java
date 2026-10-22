package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
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

    public ApartarRecursosDeReservaService(ServicioRecursoRepositoryPort servicioRecursoRepositoryPort,
                                           RecursoRepositoryPort recursoRepositoryPort,
                                           RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase) {
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.registrarPrestamoRecursoUseCase = registrarPrestamoRecursoUseCase;
    }

    // Si algún recurso falla se lanza el error. Como todo va en la misma transacción que la
    // reserva, no queda guardado ningún préstamo ni la reserva.
    @Override
    @Transactional
    public List<RecursoPrestado> ejecutar(Integer idReserva, Integer idServicio, LocalDateTime inicio,
                                          LocalDateTime fin, Collection<Integer> idsOpcionalesElegidos) {
        Set<Integer> elegidos = idsOpcionalesElegidos == null ? Set.of() : Set.copyOf(idsOpcionalesElegidos);
        List<ServicioRecurso> asociados = servicioRecursoRepositoryPort.buscarPorServicio(idServicio);

        // 1. El cliente solo puede pedir recursos que el servicio ofrece como opcionales.
        Set<Integer> opcionalesDelServicio = asociados.stream()
                .filter(a -> !a.isObligatorio())
                .map(ServicioRecurso::getIdRecurso)
                .collect(Collectors.toSet());
        List<Integer> noValidos = elegidos.stream()
                .filter(id -> !opcionalesDelServicio.contains(id))
                .sorted()
                .toList();
        if (!noValidos.isEmpty()) {
            throw new RecursoOpcionalNoValidoException(idServicio, noValidos.stream()
                    .map(this::nombreParaMensaje)
                    .toList());
        }

        // 2. Se apartan los obligatorios y los opcionales elegidos, siempre en orden de id del
        //    recurso. Así dos reservas que usan los mismos recursos los toman en el mismo orden
        //    y ninguna se queda esperando a la otra para siempre.
        List<ServicioRecurso> aApartar = asociados.stream()
                .filter(a -> a.isObligatorio() || elegidos.contains(a.getIdRecurso()))
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

    private RecursoPrestado apartar(ServicioRecurso asociacion, Integer idReserva,
                                    LocalDateTime inicio, LocalDateTime fin) {
        Integer idRecurso = asociacion.getIdRecurso();
        int cantidad = asociacion.getCantidad();

        // Se toma el recurso con bloqueo de la fila, igual que al registrar un préstamo. Si
        // está desactivado, el registro del préstamo lo rechaza más abajo.
        Recurso recurso = recursoRepositoryPort.buscarPorIdConBloqueo(idRecurso)
                .orElseThrow(() -> new RecursoNoEncontradoException(idRecurso));

        // Los obligatorios ya van incluidos en el precio del servicio; los opcionales se
        // cobran aparte.
        BigDecimal precioTotal = asociacion.isObligatorio()
                ? BigDecimal.ZERO
                : recurso.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));

        // Stock, horario y bloqueos se revisan con la misma lógica de un préstamo normal.
        try {
            return registrarPrestamoRecursoUseCase.ejecutar(new RegistrarPrestamoComando(
                    idRecurso, idReserva, cantidad, inicio, fin, precioTotal));
        } catch (DisponibilidadInsuficienteException e) {
            throw RecursoNoDisponibleParaReservaException.sinUnidades(recurso.getNombre(), cantidad, e.getDisponible());
        } catch (RecursoNoOperativoException e) {
            throw RecursoNoDisponibleParaReservaException.noOperativo(recurso.getNombre());
        }
    }
}
