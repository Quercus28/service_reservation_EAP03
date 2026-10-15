package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoConHorarios;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadProveedorUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultarDisponibilidadProveedorService implements ConsultarDisponibilidadProveedorUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    public ConsultarDisponibilidadProveedorService(RecursoRepositoryPort recursoRepositoryPort,
                                                   HorarioRecursoRepositoryPort horarioRecursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoPaginado<RecursoConHorarios> ejecutar(Integer idProveedor, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1) {
            throw new DatosInvalidosException("pagina debe ser >= 0 y tamano debe ser >= 1");
        }

        ResultadoPaginado<Recurso> recursos =
                recursoRepositoryPort.buscarActivosPorProveedor(idProveedor, pagina, tamano);

        List<Integer> ids = recursos.getContenido().stream()
                .map(Recurso::getId)
                .toList();

        // Una sola consulta para los horarios de todos los recursos de la página.
        Map<Integer, List<HorarioRecurso>> horariosPorRecurso = ids.isEmpty()
                ? Map.of()
                : horarioRecursoRepositoryPort.buscarPorRecursos(ids).stream()
                        .collect(Collectors.groupingBy(HorarioRecurso::getIdRecurso));

        List<RecursoConHorarios> contenido = recursos.getContenido().stream()
                .map(r -> new RecursoConHorarios(r, horariosPorRecurso.getOrDefault(r.getId(), List.of())))
                .toList();

        return new ResultadoPaginado<>(contenido, recursos.getPagina(), recursos.getTamano(),
                recursos.getTotalElementos());
    }
}
