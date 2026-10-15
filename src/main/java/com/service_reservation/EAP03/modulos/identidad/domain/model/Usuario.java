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
    private String secret2fa;
    private boolean is2faEnabled = false;

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

    public Usuario(Long id, String email, String passwordHash, boolean enabled, Set<String> roles, String secret2fa, boolean is2faEnabled) {
        this(id, email, passwordHash, enabled, roles);
        this.secret2fa = secret2fa;
        this.is2faEnabled = is2faEnabled;
    }
    
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isEnabled() { return enabled; }
    public Set<String> getRoles() { return Collections.unmodifiableSet(roles); }
    public String getSecret2fa() { return secret2fa; }
    public boolean is2faEnabled() { return is2faEnabled; }
}