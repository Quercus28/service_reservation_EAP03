package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.IniciarSesionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public LoginController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
        IniciarSesionComando comando = new IniciarSesionComando(request.email(), request.password());
        TokenRespuesta respuesta = iniciarSesionUseCase.ejecutar(comando);
        return ResponseEntity.ok(new LoginResponseDTO(
                respuesta.token(),
                respuesta.tipo(),
                respuesta.expiracionSegundos()
        ));
    }
}
