package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.outbound.adapter;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.ProveedorIdentidadPort;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.ProveedorSpringDataRepository;
import org.springframework.stereotype.Component;

@Component
public class ProveedorIdentidadAdapter implements ProveedorIdentidadPort {
    private final ProveedorSpringDataRepository proveedorRepository;

    public ProveedorIdentidadAdapter(ProveedorSpringDataRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public Long obtenerProveedorIdPorEmail(String email) {
        return proveedorRepository.findIdByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no corresponde a un proveedor"));
    }
}
