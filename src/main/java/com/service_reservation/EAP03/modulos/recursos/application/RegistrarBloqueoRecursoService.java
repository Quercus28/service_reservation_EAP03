package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoEnConflictoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarBloqueoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarBloqueoRecursoService implements RegistrarBloqueoRecursoUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;
    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    public RegistrarBloqueoRecursoService(RecursoRepositoryPort recursoRepositoryPort,
                                          BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort,
                                          RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.bloqueoRecursoRepositoryPort = bloqueoRecursoRepositoryPort;
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
    }

    @Override
    @Transactional
    public BloqueoRecurso ejecutar(RegistrarBloqueoComando comando) {
        // Mismo bloqueo pesimista que RegistrarPrestamoRecursoService: serializa bloqueos y
        // préstamos sobre el mismo recurso, así ninguno de los dos se cuela entre la
        // verificación del otro y su inserción.
        recursoRepositoryPort.buscarPorIdConBloqueo(comando.idRecurso())
                .orElseThrow(() -> new RecursoNoEncontradoException(comando.idRecurso()));

        BloqueoRecurso nuevo = BloqueoRecurso.nuevo(
                comando.idRecurso(),
                comando.fechaInicio(),
                comando.fechaFin(),
                comando.motivo()
        );

        bloqueoRecursoRepositoryPort.buscarQueSeSolapan(comando.idRecurso(), nuevo.getFechaInicio(), nuevo.getFechaFin())
                .stream()
                .filter(b -> b.seSolapaCon(nuevo.getFechaInicio(), nuevo.getFechaFin()))
                .findFirst()
                .ifPresent(b -> {
                    throw BloqueoEnConflictoException.conBloqueo(b);
                });

        recursoPrestadoRepositoryPort.buscarQueSeSolapan(comando.idRecurso(), nuevo.getFechaInicio(), nuevo.getFechaFin())
                .stream()
                .filter(p -> p.seSolapaCon(nuevo.getFechaInicio(), nuevo.getFechaFin()))
                .findFirst()
                .ifPresent(p -> {
                    throw BloqueoEnConflictoException.conPrestamo(p);
                });

        return bloqueoRecursoRepositoryPort.guardar(nuevo);
    }
}
