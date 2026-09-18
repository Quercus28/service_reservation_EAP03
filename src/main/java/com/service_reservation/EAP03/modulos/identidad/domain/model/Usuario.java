package com.service_reservation.EAP03.modulos.identidad.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Usuario {
    private Long id;
    private String email;
    private String passwordHash;
    private boolean enabled = true;
    private Set<String> roles = new HashSet<>();

    public Usuario(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Usuario(Long id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Usuario(Long id, String email, String passwordHash, boolean enabled, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }
    
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
    public Set<String> getRoles() { return Collections.unmodifiableSet(roles); }
}