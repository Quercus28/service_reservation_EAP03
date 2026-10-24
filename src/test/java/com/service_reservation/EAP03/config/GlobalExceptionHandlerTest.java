package com.service_reservation.EAP03.config;

import com.service_reservation.EAP03.modulos.admin.domain.exception.AdminDominioException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.DatosAdminInvalidosException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.OperacionAdminInvalidaException;
import com.service_reservation.EAP03.modulos.admin.domain.exception.RecursoAdminNoEncontradoException;
import com.service_reservation.EAP03.modulos.admin.domain.model.IntentoAccesoDenegado;
import com.service_reservation.EAP03.modulos.admin.domain.ports.in.RegistrarAccesoDenegadoUseCase;
import com.service_reservation.EAP03.modulos.agendas_horarios.application.PersistenciaAgendasHorariosNoDisponibleException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaDuplicadaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.HorarioSolapadoException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.ServicioNoPerteneceAlProveedorException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.RangoFechasExcedidoException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import com.service_reservation.EAP03.modulos.identidad.application.exception.ReglaNegocioException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CredencialesInvalidasException;
import com.service_reservation.EAP03.modulos.identidad.domain.exception.CuentaBloqueadaException;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private ObjectProvider<RegistrarAccesoDenegadoUseCase> auditUseCaseProvider;
    @Mock private RegistrarAccesoDenegadoUseCase auditUseCase;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(auditUseCaseProvider);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static void assertError(ResponseEntity<Map<String, Object>> response, int status, String errorCode, String message) {
        assertEquals(status, response.getStatusCode().value());
        assertEquals(errorCode, response.getBody().get("errorCode"));
        assertEquals(message, response.getBody().get("message"));
        assertNotNull(response.getBody().get("traceId"));
    }

    // ---------------------------------------------------------------- Identidad

    @Test
    @DisplayName("Credenciales inválidas responden 401")
    void invalidCredentialsReturns401() {
        assertError(handler.handleCredencialesInvalidas(new CredencialesInvalidasException("Credenciales incorrectas")),
                401, "CREDENCIALES_INVALIDAS", "Credenciales incorrectas");
    }

    @Test
    @DisplayName("Cuenta bloqueada responde 423")
    void lockedAccountReturns423() {
        assertError(handler.handleCuentaBloqueada(new CuentaBloqueadaException("Cuenta bloqueada")),
                423, "CUENTA_BLOQUEADA", "Cuenta bloqueada");
    }

    @Test
    @DisplayName("Una regla de negocio incumplida responde 409")
    void businessRuleReturns409() {
        assertError(handler.handleReglaNegocio(new ReglaNegocioException("El correo ya está en uso")),
                409, "REGLA_NEGOCIO", "El correo ya está en uso");
    }

    // ---------------------------------------------------------------- Agendas y horarios

    @Test
    @DisplayName("Un horario solapado responde 409")
    void overlappingScheduleReturns409() {
        assertError(handler.handleHorarioSolapado(new HorarioSolapadoException("Se solapa")),
                409, "REGLA_NEGOCIO", "Se solapa");
    }

    @Test
    @DisplayName("Una agenda inexistente responde 404")
    void agendaNotFoundReturns404() {
        AgendaNoEncontradaException ex = new AgendaNoEncontradaException(5L);

        assertError(handler.handleAgendaNoEncontrada(ex), 404, "AGENDA_NO_ENCONTRADA", ex.getMessage());
    }

    @Test
    @DisplayName("Una agenda duplicada responde 409")
    void duplicatedAgendaReturns409() {
        AgendaDuplicadaException ex = new AgendaDuplicadaException(3);

        assertError(handler.handleAgendaDuplicada(ex), 409, "AGENDA_DUPLICADA", ex.getMessage());
    }

    @Test
    @DisplayName("Un servicio que no pertenece al proveedor responde 403")
    void serviceFromAnotherProviderReturns403() {
        ServicioNoPerteneceAlProveedorException ex = new ServicioNoPerteneceAlProveedorException(3);

        assertError(handler.handleServicioNoPerteneceAlProveedor(ex), 403, "SERVICIO_NO_PERTENECE_AL_PROVEEDOR", ex.getMessage());
    }

    @Test
    @DisplayName("Datos de agenda inválidos responden 400")
    void invalidAgendaDataReturns400() {
        assertError(handler.handleDatosAgendaInvalidos(new DatosAgendaInvalidosException("Datos inválidos")),
                400, "DATOS_AGENDA_INVALIDOS", "Datos inválidos");
    }

    @Test
    @DisplayName("Persistencia de agendas no disponible responde 503")
    void agendaPersistenceUnavailableReturns503() {
        PersistenciaAgendasHorariosNoDisponibleException ex = new PersistenciaAgendasHorariosNoDisponibleException();

        assertError(handler.handlePersistenciaNoDisponible(ex), 503, "PERSISTENCIA_NO_DISPONIBLE", ex.getMessage());
    }

    // ---------------------------------------------------------------- Disponibilidad

    @Test
    @DisplayName("Parámetros de disponibilidad inválidos responden 400")
    void invalidAvailabilityReturns400() {
        assertError(handler.handleDisponibilidadInvalida(new DisponibilidadInvalidaException("Rango inválido")),
                400, "PARAMETROS_INVALIDOS", "Rango inválido");
    }

    @Test
    @DisplayName("Un rango de fechas excedido responde 400")
    void exceededDateRangeReturns400() {
        assertError(handler.handleRangoFechasExcedido(new RangoFechasExcedidoException("Máximo 30 días")),
                400, "RANGO_FECHAS_EXCEDIDO", "Máximo 30 días");
    }

    @Test
    @DisplayName("Un servicio inexistente al consultar disponibilidad responde 404")
    void availabilityServiceNotFoundReturns404() {
        ServicioParaDisponibilidadNoEncontradoException ex = new ServicioParaDisponibilidadNoEncontradoException(9);

        assertError(handler.handleServicioNoEncontrado(ex), 404, "SERVICIO_NO_ENCONTRADO", ex.getMessage());
    }

    // ---------------------------------------------------------------- Administración

    @Test
    @DisplayName("Datos de administración inválidos responden 400")
    void invalidAdminDataReturns400() {
        assertError(handler.handleDatosAdminInvalidos(new DatosAdminInvalidosException("Motivo obligatorio")),
                400, "DATOS_ADMIN_INVALIDOS", "Motivo obligatorio");
    }

    @Test
    @DisplayName("Una operación de administración inválida responde 400")
    void invalidAdminOperationReturns400() {
        assertError(handler.handleOperacionAdminInvalida(new OperacionAdminInvalidaException("Ya revocado")),
                400, "OPERACION_ADMIN_INVALIDA", "Ya revocado");
    }

    @Test
    @DisplayName("Un recurso de administración inexistente responde 404")
    void adminResourceNotFoundReturns404() {
        assertError(handler.handleRecursoAdminNoEncontrado(new RecursoAdminNoEncontradoException("No existe")),
                404, "RECURSO_ADMIN_NO_ENCONTRADO", "No existe");
    }

    @Test
    @DisplayName("Cualquier otro error de dominio de administración responde 400")
    void adminDomainErrorReturns400() {
        AdminDominioException ex = new AdminDominioException("Error de dominio") {
        };

        assertError(handler.handleAdminDominio(ex), 400, "ERROR_DOMINIO_ADMIN", "Error de dominio");
    }

    // ---------------------------------------------------------------- Acceso denegado

    @Test
    @DisplayName("Un acceso denegado fuera de /api/admin responde 403 sin auditar")
    void accessDeniedOutsideAdminIsNotAudited() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/servicios");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(new AccessDeniedException("x"), request);

        // Assert
        assertError(response, 403, "ACCESO_NO_AUTORIZADO", "No tiene privilegios para realizar esta operación");
        verifyNoInteractions(auditUseCaseProvider);
    }

    @Test
    @DisplayName("Un acceso denegado en /api/admin se audita con el id del usuario autenticado")
    void accessDeniedInAdminIsAuditedWithUserId() {
        // Arrange
        when(auditUseCaseProvider.getIfAvailable()).thenReturn(auditUseCase);
        UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(7L, "cliente@test.com", List.of("CLIENTE"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/permisos");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(new AccessDeniedException("x"), request);

        // Assert
        assertEquals(403, response.getStatusCode().value());
        ArgumentCaptor<IntentoAccesoDenegado> captor = ArgumentCaptor.forClass(IntentoAccesoDenegado.class);
        verify(auditUseCase).registrar(captor.capture());
        assertEquals(7L, captor.getValue().idUsuario());
        assertEquals("POST", captor.getValue().metodo());
        assertEquals("/api/admin/permisos", captor.getValue().endpoint());
        assertNotNull(captor.getValue().fecha());
    }

    @Test
    @DisplayName("Un acceso denegado en /api/admin sin usuario autenticado se audita sin id")
    void accessDeniedInAdminWithoutAuthenticationIsAuditedWithoutUserId() {
        // Arrange
        when(auditUseCaseProvider.getIfAvailable()).thenReturn(auditUseCase);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/solicitudes");

        // Act
        handler.handleAccessDenied(new AccessDeniedException("x"), request);

        // Assert
        ArgumentCaptor<IntentoAccesoDenegado> captor = ArgumentCaptor.forClass(IntentoAccesoDenegado.class);
        verify(auditUseCase).registrar(captor.capture());
        assertNull(captor.getValue().idUsuario());
    }

    @Test
    @DisplayName("Un acceso denegado en /api/admin con otro tipo de principal se audita sin id")
    void accessDeniedInAdminWithUnknownPrincipalIsAuditedWithoutUserId() {
        // Arrange
        when(auditUseCaseProvider.getIfAvailable()).thenReturn(auditUseCase);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonimo", null, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/solicitudes");

        // Act
        handler.handleAccessDenied(new AccessDeniedException("x"), request);

        // Assert
        ArgumentCaptor<IntentoAccesoDenegado> captor = ArgumentCaptor.forClass(IntentoAccesoDenegado.class);
        verify(auditUseCase).registrar(captor.capture());
        assertNull(captor.getValue().idUsuario());
    }

    @Test
    @DisplayName("Un acceso denegado en /api/admin responde 403 aunque no haya servicio de auditoría")
    void accessDeniedInAdminWithoutAuditService() {
        // Arrange
        when(auditUseCaseProvider.getIfAvailable()).thenReturn(null);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/solicitudes");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(new AccessDeniedException("x"), request);

        // Assert
        assertEquals(403, response.getStatusCode().value());
    }

    // ---------------------------------------------------------------- Validación y peticiones mal formadas

    @Test
    @DisplayName("Los errores globales de validación se informan con el nombre del objeto")
    @SuppressWarnings("unchecked")
    void globalValidationErrorsAreReported() throws Exception {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registro");
        bindingResult.addError(new ObjectError("registroUsuarioRequestDTO", "Las contraseñas no coinciden"));
        bindingResult.addError(new FieldError("registro", "email", "Formato de correo electrónico inválido"));
        MethodParameter parametro = new MethodParameter(Object.class.getMethod("toString"), -1);

        // Act
        ResponseEntity<Map<String, Object>> response =
                handler.handleValidations(new MethodArgumentNotValidException(parametro, bindingResult));

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDACION_ENTRADA", response.getBody().get("errorCode"));
        assertEquals("Error en los datos enviados", response.getBody().get("message"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertEquals("Las contraseñas no coinciden", details.get("registroUsuarioRequestDTO"));
        assertEquals("Formato de correo electrónico inválido", details.get("email"));
    }

    @Test
    @DisplayName("Un parámetro con formato inválido responde 400 indicando su nombre")
    void typeMismatchReturns400() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("fechaInicio");

        // Act & Assert
        assertError(handler.handleTypeMismatch(ex), 400, "FORMATO_PARAMETRO_INVALIDO",
                "El parámetro 'fechaInicio' tiene un formato o valor inválido");
    }

    @Test
    @DisplayName("Un cuerpo no legible responde 400 sin exponer el mensaje interno")
    void unreadableBodyReturns400() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/servicios");

        assertError(handler.handleCuerpoNoLegible(mock(HttpMessageNotReadableException.class), request),
                400, "CUERPO_NO_VALIDO", "El cuerpo de la petición no es válido");
    }

    @Test
    @DisplayName("Un tipo de contenido no soportado responde 415")
    void unsupportedMediaTypeReturns415() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/servicios");

        assertError(handler.handleTipoContenidoNoSoportado(new HttpMediaTypeNotSupportedException("text/plain"), request),
                415, "TIPO_CONTENIDO_NO_SOPORTADO", "El tipo de contenido no es soportado, usa application/json");
    }

    @Test
    @DisplayName("Un método no permitido responde 405")
    void methodNotAllowedReturns405() {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/servicios");

        assertError(handler.handleMetodoNoPermitido(new HttpRequestMethodNotSupportedException("PATCH"), request),
                405, "METODO_NO_PERMITIDO", "Este método no está permitido en esta ruta");
    }

    @Test
    @DisplayName("Una ruta inexistente responde 404")
    void unknownRouteReturns404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/no-existe");

        assertError(handler.handleRutaNoEncontrada(mock(NoResourceFoundException.class), request),
                404, "RUTA_NO_ENCONTRADA", "La ruta no existe");
    }
}
