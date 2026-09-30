package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroUsuarioRequestDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("TC-04: Rechaza una contrasena debil (sin mayuscula y con menos de 8 caracteres)")
    void rechazaPasswordDebil() {
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO(
                "pendienteback@gmail.com", "abc123", "abc123", "CLIENTE",
                "Nombre Apellido", "3001234567", "CC123456");

        Set<ConstraintViolation<RegistroUsuarioRequestDTO>> violaciones = validator.validate(dto);

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("TC-05: Rechaza campos obligatorios vacios, con un mensaje especifico por cada uno")
    void rechazaCamposObligatoriosVacios() {
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO(
                "", "", "", "", "", null, "");

        Set<ConstraintViolation<RegistroUsuarioRequestDTO>> violaciones = validator.validate(dto);
        Set<String> campos = violaciones.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(campos.contains("email"));
        assertTrue(campos.contains("password"));
        assertTrue(campos.contains("confirmarPassword"));
        assertTrue(campos.contains("rol"));
        assertTrue(campos.contains("nombreIdentificacion"));
        assertTrue(campos.contains("documentoIdentidad"));
    }

    @Test
    @DisplayName("TC-07: Rechaza el registro cuando no se selecciona un rol")
    void rechazaRolNoSeleccionado() {
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO(
                "pendienteback@gmail.com", "Segura123", "Segura123", "",
                "Nombre Apellido", "3001234567", "CC123456");

        Set<ConstraintViolation<RegistroUsuarioRequestDTO>> violaciones = validator.validate(dto);

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("rol")));
    }

    @Test
    @DisplayName("TC-08: Rechaza el registro cuando la confirmacion de contrasena no coincide")
    void rechazaConfirmacionNoCoincide() {
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO(
                "pendienteback@gmail.com", "Segura123", "Segura456", "CLIENTE",
                "Nombre Apellido", "3001234567", "CC123456");

        Set<ConstraintViolation<RegistroUsuarioRequestDTO>> violaciones = validator.validate(dto);

        assertTrue(violaciones.stream().anyMatch(v -> v.getMessage().equals("Las contraseñas no coinciden")));
    }

    // Prueba de control (no es uno de los TC entregados): confirma que la instancia
    // "valida" usada como base en los demas casos no dispara ninguna violacion por si sola.
    @Test
    @DisplayName("Control: un registro con todos los datos validos no produce violaciones")
    void aceptaDatosValidos() {
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO(
                "pendienteback@gmail.com", "Segura123", "Segura123", "CLIENTE",
                "Nombre Apellido", "3001234567", "CC123456");

        assertTrue(validator.validate(dto).isEmpty());
    }
}
