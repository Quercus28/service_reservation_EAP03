package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.model.ResultadoPaginado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.EliminarBloqueoRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ListarBloqueosRecursoUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarBloqueoRecursoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound.ReglasAccesoRecurso.PROPIETARIO_O_ADMIN;

@RestController
@RequestMapping("/api/v1/recursos")
public class BloqueoRecursoController {

    private final RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase;
    private final ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase;
    private final EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase;

    public BloqueoRecursoController(RegistrarBloqueoRecursoUseCase registrarBloqueoRecursoUseCase,
                                    ListarBloqueosRecursoUseCase listarBloqueosRecursoUseCase,
                                    EliminarBloqueoRecursoUseCase eliminarBloqueoRecursoUseCase) {
        this.registrarBloqueoRecursoUseCase = registrarBloqueoRecursoUseCase;
        this.listarBloqueosRecursoUseCase = listarBloqueosRecursoUseCase;
        this.eliminarBloqueoRecursoUseCase = eliminarBloqueoRecursoUseCase;
    }

    @PostMapping("/{id}/bloqueos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public BloqueoRecursoResponseDTO registrarBloqueo(@PathVariable Integer id,
                                                      @Valid @RequestBody RegistrarBloqueoRequestDTO request) {
        BloqueoRecurso bloqueo = registrarBloqueoRecursoUseCase.ejecutar(new RegistrarBloqueoComando(
                id,
                request.fechaInicio(),
                request.fechaFin(),
                request.motivo()
        ));
        return BloqueoRecursoResponseDTO.desde(bloqueo);
    }

    @GetMapping("/{id}/bloqueos")
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public PaginaResponseDTO<BloqueoRecursoResponseDTO> listarBloqueos(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {

        ResultadoPaginado<BloqueoRecurso> resultado = listarBloqueosRecursoUseCase.ejecutar(id, pagina, tamano);

        return new PaginaResponseDTO<>(
                resultado.getContenido().stream().map(BloqueoRecursoResponseDTO::desde).toList(),
                resultado.getPagina(),
                resultado.getTamano(),
                resultado.getTotalElementos(),
                resultado.getTotalPaginas()
        );
    }

    @DeleteMapping("/{id}/bloqueos/{idBloqueo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(PROPIETARIO_O_ADMIN)
    public void eliminarBloqueo(@PathVariable Integer id, @PathVariable Integer idBloqueo) {
        eliminarBloqueoRecursoUseCase.ejecutar(id, idBloqueo);
    }
}
