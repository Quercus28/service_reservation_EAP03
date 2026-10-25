package com.service_reservation.EAP03.modulos.admin.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistroAuditoriaEntityTest {

    @Test
    @DisplayName("Guarda y devuelve su id")
    void keepsId() {
        RegistroAuditoriaEntity entidad = new RegistroAuditoriaEntity();

        entidad.setId(9L);

        assertEquals(9L, entidad.getId());
    }
}
