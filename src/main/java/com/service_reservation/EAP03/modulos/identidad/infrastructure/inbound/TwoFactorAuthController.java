package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Activar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Configurar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.VerificarLogin2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y autenticación de dos factores.")
public class TwoFactorAuthController {

    private final Configurar2faUseCase configurar2faUseCase;
    private final Activar2faUseCase activar2faUseCase;
    private final VerificarLogin2faUseCase verificarLogin2faUseCase;

    public TwoFactorAuthController(
            Configurar2faUseCase configurar2faUseCase,
            Activar2faUseCase activar2faUseCase,
            VerificarLogin2faUseCase verificarLogin2faUseCase
    ) {
        this.configurar2faUseCase = configurar2faUseCase;
        this.activar2faUseCase = activar2faUseCase;
        this.verificarLogin2faUseCase = verificarLogin2faUseCase;
    }

    /**
     * Genera y devuelve la imagen PNG del código QR directamente para que Postman o el navegador
     * la rendericen en pantalla. También envía el secreto en el encabezado X-2FA-Secret.
     */
    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(
            summary = "Obtener el QR para configurar 2FA",
            description = "Genera el código QR de configuración para el usuario autenticado. La respuesta incluye el secreto en la cabecera X-2FA-Secret; debe tratarse como información confidencial."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen PNG del código QR.", content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Se requiere un JWT válido.", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<byte[]> obtenerQr(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal
    ) {
                if (principal == null) {
                        return ResponseEntity.status(401).build();
                }

        ResultadoConfiguracion2fa resultado = configurar2faUseCase.ejecutar(principal.getIdUsuario());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("X-2FA-Secret", resultado.secreto())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-2FA-Secret")
                .body(resultado.imagenQr());
    }

    /**
     * Confirma el primer código TOTP tras escanear el QR y activa definitivamente el 2FA.
     */
    @PostMapping("/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Activar autenticación de dos factores",
            description = "Valida el código TOTP generado por la aplicación autenticadora y activa 2FA para la cuenta. Requiere JWT de un administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación de dos factores activada."),
            @ApiResponse(responseCode = "400", description = "El código debe contener seis dígitos o no es válido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol de administrador.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Código TOTP de seis dígitos.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "codigo": "123456"
                    }
                    """))
    )
    public ResponseEntity<Map<String, String>> activar2fa(
            @AuthenticationPrincipal UsuarioAutenticadoPrincipal principal,
            @Valid @RequestBody Activar2faRequestDTO request
    ) {
                if (principal == null) {
                        return ResponseEntity.status(401).build();
                }

        activar2faUseCase.ejecutar(principal.getIdUsuario(), request.codigo());
        return ResponseEntity.ok(Map.of(
                "message", "Autenticación de dos factores activada exitosamente"
        ));
    }

    /**
     * Paso 2 del Login: valida el token temporal emitido en el login básico y el código TOTP,
     * devolviendo el JWT definitivo con los roles correspondientes.
     */
    @PostMapping("/verificar-login")
    @Operation(
            summary = "Completar inicio de sesión con 2FA",
            description = "Intercambia el token temporal obtenido en /api/v1/auth/login y el código TOTP por el JWT de acceso. No requiere un JWT previo."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación correcta; devuelve el JWT definitivo.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class), examples = @ExampleObject(value = """
                    {
                      "token": "<jwt-de-acceso>",
                      "tipo": "Bearer",
                      "expiracionSegundos": 3600,
                      "requiere2fa": false,
                      "tokenTemporal": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "El token temporal o el código no cumplen el formato requerido.", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Token temporal o código 2FA inválido o expirado.", content = @Content(mediaType = "application/json"))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token temporal y código TOTP.",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "tokenTemporal": "<token-temporal-devuelto-por-login>",
                      "codigo": "123456"
                    }
                    """))
    )
    public ResponseEntity<LoginResponseDTO> verificarLogin(
            @Valid @RequestBody Verificar2faLoginRequestDTO request
    ) {
        TokenRespuesta respuesta = verificarLogin2faUseCase.ejecutar(request.tokenTemporal(), request.codigo());

        return ResponseEntity.ok(new LoginResponseDTO(
                respuesta.token(),
                respuesta.tipo(),
                respuesta.expiracionSegundos(),
                false,
                null
        ));
    }
}
