package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.HorarioRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarHorarioComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarHorarioRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.HorarioRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarHorarioRecursoService implements RegistrarHorarioRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final HorarioRecursoRepositoryPort horarioRecursoRepositoryPort;

    public RegistrarHorarioRecursoService(RecursoRepositoryPort recursoRepositoryPort,
                                          HorarioRecursoRepositoryPort horarioRecursoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.horarioRecursoRepositoryPort = horarioRecursoRepositoryPort;
    }

    @Override
    @Transactional
    public HorarioRecurso ejecutar(RegistrarHorarioComando comando) {
        // Bloqueo pesimista sobre la fila del recurso: dos registros concurrentes de franjas
        // solapadas para el mismo recurso no pueden pasar ambos la verificación.
        recursoRepositoryPort.buscarPorIdConBloqueo(comando.idRecurso())
                .orElseThrow(() -> new RecursoNoEncontradoException(comando.idRecurso()));

        HorarioRecurso nuevo = HorarioRecurso.nuevo(
                comando.idRecurso(),
                comando.diaSemana(),
                comando.horaInicio(),
                comando.horaFin()
        );

        horarioRecursoRepositoryPort.buscarPorRecurso(comando.idRecurso()).stream()
                .filter(nuevo::seSolapaCon)
                .findFirst()
                .ifPresent(existente -> {
                    throw new HorarioSolapadoException(existente);
                });

        return horarioRecursoRepositoryPort.guardar(nuevo);
    }
}
