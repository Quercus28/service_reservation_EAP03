package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public record CrearReservaRequestDTO(

        @NotNull(message = "El servicio es obligatorio")
        Integer idServicio,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        LocalDateTime fechaInicio,

        // Igual que el motivo de los bloqueos en recursos: solo se aceptan letras (también
        // con tilde, ü y ñ), números, espacios y . , - ( ). Así se rechaza texto con código,
        // etiquetas HTML como <script> o saltos de línea.
        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        @Pattern(regexp = "[A-Za-z\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F10-9 .,()\\-]*",
                message = "Las notas solo pueden contener letras, números, espacios y los caracteres . , - ( )")
        String notas,

        // Recursos extra que el cliente quiere (ids de recurso). Es opcional.
        @Size(max = 20, message = "No se pueden pedir más de 20 recursos opcionales")
        List<@NotNull(message = "Los recursos opcionales no pueden tener valores vacíos") Integer> recursosOpcionales
) {

    @AssertTrue(message = "Los recursos opcionales no pueden repetirse")
    public boolean isRecursosOpcionalesSinRepetir() {
        return recursosOpcionales == null || new HashSet<>(recursosOpcionales).size() == recursosOpcionales.size();
    }
}
