package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private final RecursoRepositoryPort recursoRepositoryPort;
    private final RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    public ConsultarDisponibilidadService(RecursoRepositoryPort recursoRepositoryPort,
                                          RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort) {
        this.recursoRepositoryPort = recursoRepositoryPort;
        this.recursoPrestadoRepositoryPort = recursoPrestadoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadRecurso ejecutar(Integer idRecurso, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new DatosInvalidosException("El rango de fechas es invalido: 'hasta' debe ser posterior a 'desde'");
        }

        Recurso recurso = recursoRepositoryPort.buscarPorId(idRecurso)
                .orElseThrow(() -> new RecursoNoEncontradoException(idRecurso));

        List<RecursoPrestado> posiblesChoques =
                recursoPrestadoRepositoryPort.buscarQueSeSolapan(idRecurso, desde, hasta);

        int comprometido = 0;
        for (RecursoPrestado prestamo : posiblesChoques) {
            if (prestamo.seSolapaCon(desde, hasta)) {
                comprometido += prestamo.getCantidad();
            }
        }

        int disponible = recurso.unidadesDisponibles(comprometido);

        return new DisponibilidadRecurso(
                idRecurso,
                desde,
                hasta,
                recurso.getStock(),
                comprometido,
                disponible
        );
    }
}
