package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.Valid;
import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.RegistrarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y autenticación de dos factores.")
public class RegistroController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public RegistroController(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @PostMapping("/registro")
    @Operation(
            summary = "Registrar una cuenta",
            description = "Crea una cuenta de cliente o proveedor. La contraseña debe tener al menos 8 caracteres e incluir minúscula, mayúscula y número. No requiere token."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada."),
            @ApiResponse(responseCode = "400", description = "Los datos no cumplen las validaciones o las contraseñas no coinciden.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "El correo o documento ya está registrado.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos requeridos para crear una cuenta.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "email": "persona@ejemplo.com",
                      "password": "ClaveSegura123",
                      "confirmarPassword": "ClaveSegura123",
                      "rol": "CLIENTE",
                      "nombreIdentificacion": "Ana Perez",
                      "telefono": "3001234567",
                      "documentoIdentidad": "CC12345678"
                    }
                    """))
    )
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