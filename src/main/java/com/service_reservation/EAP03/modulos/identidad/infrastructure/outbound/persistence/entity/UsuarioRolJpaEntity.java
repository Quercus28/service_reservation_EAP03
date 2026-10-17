package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

// Fila de USUARIO_ROL con su columna "activo". La relación @ManyToMany de UsuarioJpaEntity
// solo sirve para leer los roles: no puede guardar "activo". Por eso, al registrar, la fila
// se guarda con esta entidad, ya con el valor de "activo" que le toca.
@Entity
@Table(name = "USUARIO_ROL")
@IdClass(UsuarioRolJpaEntity.Id.class)
public class UsuarioRolJpaEntity {

    @jakarta.persistence.Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @jakarta.persistence.Id
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected UsuarioRolJpaEntity() {
    }

    public UsuarioRolJpaEntity(Long idUsuario, Long idRol, boolean activo) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.activo = activo;
    }

    public Long getIdUsuario() { return idUsuario; }
    public Long getIdRol() { return idRol; }
    public boolean isActivo() { return activo; }

    public static class Id implements Serializable {
        private Long idUsuario;
        private Long idRol;

        public Id() {
        }

        public Id(Long idUsuario, Long idRol) {
            this.idUsuario = idUsuario;
            this.idRol = idRol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id otro)) return false;
            return Objects.equals(idUsuario, otro.idUsuario) && Objects.equals(idRol, otro.idRol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idUsuario, idRol);
        }
    }
}
