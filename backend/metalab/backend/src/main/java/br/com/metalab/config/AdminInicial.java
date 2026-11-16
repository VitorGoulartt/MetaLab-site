package br.com.metalab.config;

import br.com.metalab.model.Administrador;
import br.com.metalab.model.Perfil;
import br.com.metalab.repository.AdministradorRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador na partida, só se ainda não existir nenhum.
 * Os dados vêm de ADMIN_EMAIL e ADMIN_SENHA, então nenhuma senha fica escrita no código.
 */
@Component
public class AdminInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final AdministradorRepository administradores;
    private final PasswordEncoder encoder;
    private final Clock clock;
    private final String email;
    private final String senha;
    private final String nome;

    public AdminInicial(AdministradorRepository administradores, PasswordEncoder encoder, Clock clock,
                        @Value("${metalab.admin.email:}") String email,
                        @Value("${metalab.admin.senha:}") String senha,
                        @Value("${metalab.admin.nome:Administrador}") String nome) {
        this.administradores = administradores;
        this.encoder = encoder;
        this.clock = clock;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (administradores.existsByPerfil(Perfil.ADMIN)) {
            return;
        }
        if (email.isBlank() || senha.isBlank()) {
            log.warn("Não há administrador e ADMIN_EMAIL/ADMIN_SENHA não foram definidos: ninguém conseguirá entrar no painel.");
            return;
        }
        if (senha.length() < 10) {
            throw new IllegalStateException("ADMIN_SENHA precisa ter pelo menos 10 caracteres.");
        }
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        administradores.save(new Administrador(nome.trim(), emailNormalizado, encoder.encode(senha), Perfil.ADMIN, Instant.now(clock)));
        log.info("Administrador inicial criado: {}", emailNormalizado);
    }
}
