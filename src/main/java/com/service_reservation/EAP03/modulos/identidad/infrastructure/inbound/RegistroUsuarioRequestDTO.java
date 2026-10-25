package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.validation.PasswordsMatch;

@PasswordsMatch
public record RegistroUsuarioRequestDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    String email,
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[a-z]).{8,}$", 
             message = "La contraseña debe tener mínimo 8 caracteres, incluir números y mayúsculas")
    String password,
    
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    String confirmarPassword,
    
    @NotBlank(message = "Debe seleccionar un rol")
    @Pattern(regexp = "^(ROLE_)?(CLIENTE|PROVEEDOR)$", message = "Rol no válido")
    String rol,

    @NotBlank(message = "El nombre o razón social es obligatorio")
    @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()'\\-]*",
             message = "Caracteres no permitidos")
    String nombreIdentificacion,

    @Pattern(regexp = "^\\d{7,10}$", message = "El teléfono debe contener entre 7 y 10 dígitos")
    String telefono,

    @NotBlank(message = "El documento o NIT es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Formato de documento inválido")
    String documentoIdentidad
) {}