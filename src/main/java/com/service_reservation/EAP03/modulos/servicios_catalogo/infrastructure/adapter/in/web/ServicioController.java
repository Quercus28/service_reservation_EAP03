package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.in.web;

import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.CrearServicioRequest;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.CrearServicioUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final CrearServicioUseCase crearServicioUseCase;

    public ServicioController(CrearServicioUseCase crearServicioUseCase) {
        this.crearServicioUseCase = Objects.requireNonNull(crearServicioUseCase, "crearServicioUseCase no puede ser nulo");
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crearServicio(
            @Valid @RequestBody CrearServicioRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        ServicioResponse response = crearServicioUseCase.ejecutar(request);

        URI location = uriComponentsBuilder
                .path("/api/v1/servicios/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
