package com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.infrastructure.outbound.persistence.entity.RecursoPrestadoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecursoPrestadoMapper {

    public RecursoPrestadoJpaEntity aEntidad(RecursoPrestado prestamo) {
        RecursoPrestadoJpaEntity entidad = new RecursoPrestadoJpaEntity();
        entidad.setId(prestamo.getId());
        entidad.setIdRecurso(prestamo.getIdRecurso());
        entidad.setIdServicioPrestado(prestamo.getIdServicioPrestado());
        entidad.setCantidad(prestamo.getCantidad());
        entidad.setFechaInicio(prestamo.getFechaInicio());
        entidad.setFechaFin(prestamo.getFechaFin());
        entidad.setPrecioTotal(prestamo.getPrecioTotal());
        return entidad;
    }

    public RecursoPrestado aDominio(RecursoPrestadoJpaEntity entidad) {
        return RecursoPrestado.reconstruir(
                entidad.getId(),
                entidad.getIdRecurso(),
                entidad.getIdServicioPrestado(),
                entidad.getCantidad(),
                entidad.getFechaInicio(),
                entidad.getFechaFin(),
                entidad.getPrecioTotal()
        );
    }
}
