package br.com.metalab.security;

import br.com.metalab.model.Administrador;
import br.com.metalab.repository.AdministradorRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carrega o usuário do painel a partir da tabela administradores. O perfil vira ROLE_ADMIN, ROLE_OPERADOR ou ROLE_SUPORTE. */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdministradorRepository administradores;

    public AdminUserDetailsService(AdministradorRepository administradores) {
        this.administradores = administradores;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Administrador a = administradores.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return User.withUsername(a.getEmail())
                .password(a.getSenhaHash())
                .roles(a.getPerfil().name())
                .disabled(!a.isAtivo())
                .build();
    }
}
