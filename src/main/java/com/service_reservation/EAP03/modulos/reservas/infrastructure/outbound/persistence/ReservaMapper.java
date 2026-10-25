package com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.reservas.domain.model.Cancelacion;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.outbound.persistence.entity.ReservaJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public ReservaJpaEntity aEntidad(Reserva reserva) {
        ReservaJpaEntity entidad = new ReservaJpaEntity();
        entidad.setId(reserva.getId());
        entidad.setIdCliente(reserva.getIdCliente());
        entidad.setIdServicio(reserva.getIdServicio());
        entidad.setIdProveedor(reserva.getIdProveedor());
        entidad.setFechaInicio(reserva.getFechaInicio());
        entidad.setFechaFin(reserva.getFechaFin());
        entidad.setEstado(reserva.getEstado());
        entidad.setNotas(reserva.getNotas());
        entidad.setCreatedAt(reserva.getCreatedAt());
        entidad.setCanceladaEn(reserva.getCanceladaEn());
        entidad.setMotivoCancelacion(reserva.getMotivoCancelacion());
        return entidad;
    }

    public Reserva aDominio(ReservaJpaEntity entidad) {
        return Reserva.reconstruir(
                entidad.getId(),
                new PartesReserva(entidad.getIdCliente(), entidad.getIdServicio(), entidad.getIdProveedor()),
                new IntervaloReservado(entidad.getFechaInicio(), entidad.getFechaFin()),
                entidad.getEstado(),
                entidad.getNotas(),
                entidad.getCreatedAt(),
                new Cancelacion(entidad.getCanceladaEn(), entidad.getMotivoCancelacion())
        );
    }
}
