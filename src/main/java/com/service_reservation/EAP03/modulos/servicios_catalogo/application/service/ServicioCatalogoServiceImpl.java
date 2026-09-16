package com.service_reservation.EAP03.modulos.servicios_catalogo.application.service;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.CrearServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ServicioCatalogoServiceImpl implements CrearServicioUseCase {

    private final ServicioRepository servicioRepository;

    public ServicioCatalogoServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = Objects.requireNonNull(servicioRepository, "servicioRepository no puede ser nulo");
    }

    @Override
    @Transactional
    public ServicioResponse ejecutar(CrearServicioRequest request) {
        Objects.requireNonNull(request, "El request para crear servicio no puede ser nulo");

        Servicio nuevoServicio = Servicio.crearNuevo(
                request.idProveedor(),
                request.nombre(),
                request.duracionMinutos()
        );

        Servicio servicioGuardado = servicioRepository.guardar(nuevoServicio);

        return ServicioResponse.fromDomain(servicioGuardado);
    }
}
