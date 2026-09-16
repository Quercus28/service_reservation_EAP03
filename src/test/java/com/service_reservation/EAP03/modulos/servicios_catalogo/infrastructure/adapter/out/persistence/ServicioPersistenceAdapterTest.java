package com.service_reservation.EAP03.modulos.servicios_catalogo.infrastructure.adapter.out.persistence;

import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.model.Servicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ServicioPersistenceAdapterTest {

    @Autowired
    private ServicioPersistenceAdapter persistenceAdapter;

    @Test
    @DisplayName("Debe persistir y recuperar un servicio usando el adaptador de persistencia")
    void debeGuardarYRecuperarServicio() {
        Servicio nuevoServicio = Servicio.crearNuevo(1, "Instalación Eléctrica", 60);

        Servicio servicioGuardado = persistenceAdapter.guardar(nuevoServicio);

        assertNotNull(servicioGuardado);
        assertNotNull(servicioGuardado.getId());
        assertEquals(1, servicioGuardado.getIdProveedor());
        assertEquals("Instalación Eléctrica", servicioGuardado.getNombre());
        assertEquals(60, servicioGuardado.getDuracionMinutos());
        assertNotNull(servicioGuardado.getCreatedAt());

        Optional<Servicio> servicioEncontrado = persistenceAdapter.buscarPorId(servicioGuardado.getId());
        assertTrue(servicioEncontrado.isPresent());
        assertEquals("Instalación Eléctrica", servicioEncontrado.get().getNombre());

        List<Servicio> serviciosProveedor = persistenceAdapter.buscarPorProveedor(1);
        assertEquals(1, serviciosProveedor.size());
        assertEquals(servicioGuardado.getId(), serviciosProveedor.get(0).getId());
    }
}
