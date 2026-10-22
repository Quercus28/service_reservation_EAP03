package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.model.AgendaDeRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ServicioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Turno;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.FiltrarTurnosConRecursosDisponiblesUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.ServicioRecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FiltrarTurnosConRecursosDisponiblesService implements FiltrarTurnosConRecursosDisponiblesUseCase {

    private final ServicioRecursoRepositoryPort servicioRecursoRepositoryPort;
    private final RecursoRepositoryPort recursoRepositoryPort;
    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;
    private final BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;
    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    public FiltrarTurnosConRecursosDisponiblesService(ServicioRecursoRepositoryPort servicioRecursoRepositoryPort,
                                                      RecursoRepositoryPort recursoRepositoryPort,
                                                      HorarioRecursoRepositoryPort horarioRecursoRepositoryPort,
                                                      BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort,
                                                      RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort) {
        this.servicioRecursoRepositoryPort = servicioRecursoRepositoryPort;
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
        this.bloqueoRecursoRepositoryPort = bloqueoRecursoRepositoryPort;
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
    }

    // Se consulta la base una sola vez por cada tipo de dato (recursos, horarios, bloqueos y
    // préstamos) para todo el rango de los turnos, y después cada turno se revisa en memoria.
    @Override
    @Transactional(readOnly = true)
    public List<Turno> ejecutar(Integer idServicio, List<Turno> turnos) {
        if (turnos == null || turnos.isEmpty()) {
            return turnos == null ? List.of() : turnos;
        }

        List<ServicioRecurso> obligatorios = servicioRecursoRepositoryPort.buscarPorServicio(idServicio).stream()
                .filter(ServicioRecurso::isObligatorio)
                .toList();
        if (obligatorios.isEmpty()) {
            return turnos;
        }

        List<Integer> ids = obligatorios.stream().map(ServicioRecurso::getIdRecurso).toList();
        LocalDateTime desde = turnos.stream().map(Turno::inicio).min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime hasta = turnos.stream().map(Turno::fin).max(Comparator.naturalOrder()).orElseThrow();

        Map<Integer, Recurso> recursos = recursoRepositoryPort.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Recurso::getId, Function.identity()));
        Map<Integer, List<HorarioRecurso>> horarios = horarioRecursoRepositoryPort.buscarPorRecursos(ids).stream()
                .collect(Collectors.groupingBy(HorarioRecurso::getIdRecurso));
        Map<Integer, List<BloqueoRecurso>> bloqueos =
                bloqueoRecursoRepositoryPort.buscarQueSeSolapanDeRecursos(ids, desde, hasta).stream()
                        .collect(Collectors.groupingBy(BloqueoRecurso::getIdRecurso));
        Map<Integer, List<RecursoPrestado>> prestamos =
                recursoPrestadoRepositoryPort.buscarQueSeSolapanDeRecursos(ids, desde, hasta).stream()
                        .collect(Collectors.groupingBy(RecursoPrestado::getIdRecurso));

        // Un recurso que ya no existe no se puede usar: sus turnos se quitan.
        Map<Integer, AgendaDeRecurso> agendas = recursos.values().stream()
                .collect(Collectors.toMap(Recurso::getId, r -> new AgendaDeRecurso(
                        r,
                        horarios.get(r.getId()),
                        bloqueos.get(r.getId()),
                        prestamos.get(r.getId()))));

        return turnos.stream()
                .filter(turno -> obligatorios.stream().allMatch(asociacion -> {
                    AgendaDeRecurso agenda = agendas.get(asociacion.getIdRecurso());
                    return agenda != null
                            && agenda.puedeAtender(asociacion.getCantidad(), turno.inicio(), turno.fin());
                }))
                .toList();
    }
}
