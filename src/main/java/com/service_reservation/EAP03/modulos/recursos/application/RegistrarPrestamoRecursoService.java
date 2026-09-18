package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DisponibilidadInsuficienteException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarPrestamoRecursoService implements RegistrarPrestamoRecursoUseCase {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;
    private final RecursoRepositoryPort recursoRepositoryPort;

    public RegistrarPrestamoRecursoService(ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                                           RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort,
                                           RecursoRepositoryPort recursoRepositoryPort) {
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
        this.recursoRepositoryPort = recursoRepositoryPort;
    }

    @Override
    @Transactional
    public RecursoPrestado ejecutar(RegistrarPrestamoComando comando) {
        // Bloqueo pesimista sobre la fila del recurso: serializa las peticiones
        // concurrentes sobre el mismo recurso para evitar sobre-reserva.
        recursoRepositoryPort.buscarPorIdConBloqueo(comando.idRecurso())
                .orElseThrow(() -> new RecursoNoEncontradoException(comando.idRecurso()));

        DisponibilidadRecurso disponibilidad = consultarDisponibilidadUseCase.ejecutar(
                comando.idRecurso(),
                comando.fechaInicio(),
                comando.fechaFin()
        );

        if (comando.cantidad() > disponibilidad.disponible()) {
            throw new DisponibilidadInsuficienteException(
                    comando.idRecurso(),
                    comando.cantidad(),
                    disponibilidad.disponible()
            );
        }

        RecursoPrestado prestamo = RecursoPrestado.nuevo(
                comando.idRecurso(),
                comando.idServicioPrestado(),
                comando.cantidad(),
                comando.fechaInicio(),
                comando.fechaFin(),
                comando.precioTotal()
        );

        return recursoPrestadoRepositoryPort.guardar(prestamo);
    }
}
