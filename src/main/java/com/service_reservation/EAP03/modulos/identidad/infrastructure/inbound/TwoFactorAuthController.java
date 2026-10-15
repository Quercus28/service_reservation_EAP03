package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.ResultadoConfiguracion2fa;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Activar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.Configurar2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.VerificarLogin2faUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
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
