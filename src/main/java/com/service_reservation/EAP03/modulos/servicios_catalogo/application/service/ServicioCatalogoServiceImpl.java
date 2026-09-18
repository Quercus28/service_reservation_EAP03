package com.service_reservation.EAP03.modulos.servicios_catalogo.application.service;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ActualizarServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.PaginaResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ActualizarServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ConsultarServiciosUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.CrearServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.EliminarServicioUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ServicioCatalogoServiceImpl implements
        CrearServicioUseCase,
        ActualizarServicioUseCase,
        ObtenerServicioPorIdUseCase,
        ConsultarServiciosUseCase,
        EliminarServicioUseCase {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

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

    @Override
    @Transactional
    public ServicioResponse ejecutar(Integer id, ActualizarServicioRequest request) {
        Objects.requireNonNull(id, "El id del servicio no puede ser nulo");
        Objects.requireNonNull(request, "El request para actualizar servicio no puede ser nulo");

        Servicio servicio = servicioRepository.buscarPorId(id)
                .orElseThrow(() -> new ServicioNoEncontradoException(id));

        servicio.actualizar(request.nombre(), request.duracionMinutos());

        Servicio servicioActualizado = servicioRepository.guardar(servicio);

        return ServicioResponse.fromDomain(servicioActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponse ejecutar(Integer id) {
        if (id == null) {
            throw new ServicioNoEncontradoException("El id del servicio es obligatorio");
        }

        Servicio servicio = servicioRepository.buscarPorId(id)
                .orElseThrow(() -> new ServicioNoEncontradoException(id));

        return ServicioResponse.fromDomain(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ServicioResponse> ejecutar(Integer idProveedor, String nombre, int pagina, int tamano) {
        int paginaValida = Math.max(pagina, 0);
        int tamanoValido = tamano <= 0 ? DEFAULT_SIZE : Math.min(tamano, MAX_PAGE_SIZE);

        ResultadoPaginado<Servicio> resultado = servicioRepository.buscarPaginado(
                idProveedor,
                nombre,
                paginaValida,
                tamanoValido
        );

        List<ServicioResponse> elementosResponse = resultado.elementos().stream()
                .map(ServicioResponse::fromDomain)
                .toList();

        return PaginaResponse.de(
                elementosResponse,
                resultado.pagina(),
                resultado.tamano(),
                resultado.totalElementos(),
                resultado.totalPaginas()
        );
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (id == null || !servicioRepository.existePorId(id)) {
            throw new ServicioNoEncontradoException(id);
        }

        servicioRepository.eliminarPorId(id);
    }
}
