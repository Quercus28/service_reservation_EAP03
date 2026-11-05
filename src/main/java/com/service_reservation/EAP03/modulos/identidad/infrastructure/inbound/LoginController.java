package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.IniciarSesionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y autenticación de dos factores.")
public class LoginController {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public LoginController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida el correo y la contraseña. Los administradores siempre deben completar MFA. Devuelve un token temporal y un mensaje para configurar o verificar el factor; el JWT de acceso solo se entrega al completar MFA."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas; la respuesta indica si se requiere completar 2FA.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class), examples = @ExampleObject(value = """
                    {
                      "token": null,
                      "tipo": null,
                      "expiracionSegundos": 0,
                      "requiere2fa": true,
                      "tokenTemporal": "<token-temporal>",
                      "mensaje": "Verifique el código en su autenticador"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "El correo o la contraseña no cumplen el formato requerido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o cuenta no autenticable.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "423", description = "La cuenta está bloqueada.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales de la cuenta.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "email": "persona@ejemplo.com",
                      "password": "ClaveSegura123"
                    }
                    """))
    )
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO request) {
        IniciarSesionComando comando = new IniciarSesionComando(request.email(), request.password());
        TokenRespuesta respuesta = iniciarSesionUseCase.ejecutar(comando);
        return ResponseEntity.ok(new LoginResponseDTO(
                respuesta.token(),
                respuesta.tipo(),
                respuesta.expiracionSegundos(),
                respuesta.requiere2fa(),
                respuesta.tokenTemporal(),
                respuesta.mensaje()
        ));
    }
}
