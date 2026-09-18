package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.*;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recursos")
public class RecursoController {

    private final CrearRecursoUseCase crearRecursoUseCase;
    private final ActualizarRecursoUseCase actualizarRecursoUseCase;
    private final ConsultarRecursosUseCase consultarRecursosUseCase;
    private final ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase;
    private final DesactivarRecursoUseCase desactivarRecursoUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;

    public RecursoController(CrearRecursoUseCase crearRecursoUseCase,
                             ActualizarRecursoUseCase actualizarRecursoUseCase,
                             ConsultarRecursosUseCase consultarRecursosUseCase,
                             ObtenerRecursoPorIdUseCase obtenerRecursoPorIdUseCase,
                             DesactivarRecursoUseCase desactivarRecursoUseCase,
                             ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                             RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase) {
        this.crearRecursoUseCase = crearRecursoUseCase;
        this.actualizarRecursoUseCase = actualizarRecursoUseCase;
        this.consultarRecursosUseCase = consultarRecursosUseCase;
        this.obtenerRecursoPorIdUseCase = obtenerRecursoPorIdUseCase;
        this.desactivarRecursoUseCase = desactivarRecursoUseCase;
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.registrarPrestamoRecursoUseCase = registrarPrestamoRecursoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecursoResponseDTO crear(@Valid @RequestBody CrearRecursoRequestDTO request) {
        Recurso recurso = crearRecursoUseCase.ejecutar(new CrearRecursoComando(
                request.idProveedor(),
                request.nombre(),
                request.precioUnitario(),
                request.stock()
        ));
        return RecursoResponseDTO.desde(recurso);
    }

    @GetMapping
    public PaginaResponseDTO<RecursoResponseDTO> listar(
            @RequestParam Integer idProveedor,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {

        ResultadoPaginado<Recurso> resultado = consultarRecursosUseCase.ejecutar(idProveedor, pagina, tamano);

        List<RecursoResponseDTO> contenido = resultado.getContenido().stream()
                .map(RecursoResponseDTO::desde)
                .toList();

        return new PaginaResponseDTO<>(
                contenido,
                resultado.getPagina(),
                resultado.getTamano(),
                resultado.getTotalElementos(),
                resultado.getTotalPaginas()
        );
    }

    @GetMapping("/{id}")
    public RecursoResponseDTO obtener(@PathVariable Integer id) {
        return RecursoResponseDTO.desde(obtenerRecursoPorIdUseCase.ejecutar(id));
    }

    @PutMapping("/{id}")
    public RecursoResponseDTO actualizar(@PathVariable Integer id,
                                         @Valid @RequestBody ActualizarRecursoRequestDTO request) {
        Recurso recurso = actualizarRecursoUseCase.ejecutar(new ActualizarRecursoComando(
                id,
                request.nombre(),
                request.precioUnitario(),
                request.stock()
        ));
        return RecursoResponseDTO.desde(recurso);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Integer id) {
        desactivarRecursoUseCase.ejecutar(id);
    }

    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponseDTO consultarDisponibilidad(
            @PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        return DisponibilidadResponseDTO.desde(
                consultarDisponibilidadUseCase.ejecutar(id, desde, hasta));
    }

    @PostMapping("/{id}/prestamos")
    @ResponseStatus(HttpStatus.CREATED)
    public RecursoPrestadoResponseDTO registrarPrestamo(
            @PathVariable Integer id,
            @Valid @RequestBody RegistrarPrestamoRequestDTO request) {

        RecursoPrestado prestamo = registrarPrestamoRecursoUseCase.ejecutar(new RegistrarPrestamoComando(
                id,
                request.idServicioPrestado(),
                request.cantidad(),
                request.fechaInicio(),
                request.fechaFin(),
                request.precioTotal()
        ));
        return RecursoPrestadoResponseDTO.desde(prestamo);
    }
}
