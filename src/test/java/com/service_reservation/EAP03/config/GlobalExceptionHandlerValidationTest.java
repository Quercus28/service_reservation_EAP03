package com.service_reservation.EAP03.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerValidationTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(null);

    private static FieldError error(String campo, String codigo, String mensaje) {
        return new FieldError("registro", campo, "", false, new String[]{codigo}, null, mensaje);
    }

    private static MethodArgumentNotValidException excepcionCon(FieldError... errores) throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registro");
        for (FieldError e : errores) {
            bindingResult.addError(e);
        }
        MethodParameter parametro = new MethodParameter(Object.class.getMethod("toString"), -1);
        return new MethodArgumentNotValidException(parametro, bindingResult);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> details(MethodArgumentNotValidException ex) {
        ResponseEntity<Map<String, Object>> response = handler.handleValidations(ex);
        return (Map<String, String>) response.getBody().get("details");
    }

    @Test
    @DisplayName("TC-07: con rol vacío responde 'Debe seleccionar un rol' aunque @Pattern llegue después")
    void prefersRequiredMessageWhenItComesFirst() throws Exception {
        // Arrange
        MethodArgumentNotValidException ex = excepcionCon(
                error("rol", "NotBlank", "Debe seleccionar un rol"),
                error("rol", "Pattern", "Rol no válido"));

        // Act
        Map<String, String> details = details(ex);

        // Assert
        assertEquals("Debe seleccionar un rol", details.get("rol"));
    }

    @Test
    @DisplayName("TC-07: con rol vacío responde 'Debe seleccionar un rol' aunque @Pattern llegue primero")
    void prefersRequiredMessageWhenItComesLast() throws Exception {
        // Arrange
        MethodArgumentNotValidException ex = excepcionCon(
                error("rol", "Pattern", "Rol no válido"),
                error("rol", "NotBlank", "Debe seleccionar un rol"));

        // Act
        Map<String, String> details = details(ex);

        // Assert
        assertEquals("Debe seleccionar un rol", details.get("rol"));
    }

    @Test
    @DisplayName("Sin regla de obligatorio, elige siempre el mismo mensaje sin importar el orden")
    void picksSameMessageRegardlessOfOrder() throws Exception {
        // Arrange
        MethodArgumentNotValidException enUnOrden = excepcionCon(
                error("nombre", "Size", "El nombre no puede superar los 150 caracteres"),
                error("nombre", "Pattern", "El nombre tiene caracteres no permitidos"));
        MethodArgumentNotValidException enOtroOrden = excepcionCon(
                error("nombre", "Pattern", "El nombre tiene caracteres no permitidos"),
                error("nombre", "Size", "El nombre no puede superar los 150 caracteres"));

        // Act
        String mensaje1 = details(enUnOrden).get("nombre");
        String mensaje2 = details(enOtroOrden).get("nombre");

        // Assert
        assertEquals(mensaje1, mensaje2);
    }

    @Test
    @DisplayName("Conserva un mensaje por cada campo con error")
    void keepsOneMessagePerField() throws Exception {
        // Arrange
        MethodArgumentNotValidException ex = excepcionCon(
                error("password", "Pattern", "La contraseña debe tener mínimo 8 caracteres, incluir números y mayúsculas"),
                error("password", "NotBlank", "La contraseña es obligatoria"),
                error("email", "Email", "Formato de correo electrónico inválido"));

        // Act
        Map<String, String> details = details(ex);

        // Assert
        assertEquals(2, details.size());
        assertEquals("La contraseña es obligatoria", details.get("password"));
        assertEquals("Formato de correo electrónico inválido", details.get("email"));
    }
}
