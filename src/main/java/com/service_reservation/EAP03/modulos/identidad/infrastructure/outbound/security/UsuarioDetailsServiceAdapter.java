package com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.security;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.RolJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.entity.UsuarioJpaEntity;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.outbound.persistence.repository.UsuarioSpringDataRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UsuarioDetailsServiceAdapter implements UserDetailsService {
    private final UsuarioSpringDataRepository usuarioRepository;

    public UsuarioDetailsServiceAdapter(UsuarioSpringDataRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioJpaEntity usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .disabled(!usuario.isEnabled())
                .authorities(usuario.getRoles().stream()
                        .map(RolJpaEntity::getNombre)
                        .map(nombre -> new SimpleGrantedAuthority(nombre))
                        .collect(Collectors.toSet()))
                .build();
    }
}