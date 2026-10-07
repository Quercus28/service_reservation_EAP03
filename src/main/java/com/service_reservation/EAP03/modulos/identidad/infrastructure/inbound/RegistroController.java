package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.Valid;
import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.RegistrarUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistroController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public RegistroController(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @PostMapping("/registro")
    public ResponseEntity<Void> registrarUsuario(@Valid @RequestBody RegistroUsuarioRequestDTO request) {
        UsuarioNuevoComando comando = new UsuarioNuevoComando(
            request.email(), 
            request.password(), 
            request.rol(), 
            request.nombreIdentificacion(),
            request.telefono(),
            request.documentoIdentidad()
        );
        
        registrarUsuarioUseCase.ejecutar(comando);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}