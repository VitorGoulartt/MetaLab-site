
package br.com.metalab.service;

import br.com.metalab.controller.UsuarioAdminResposta;
import br.com.metalab.controller.UsuarioAtualizacaoRequest;
import br.com.metalab.controller.UsuarioCriacaoRequest;
import br.com.metalab.model.Administrador;
import br.com.metalab.model.Perfil;
import br.com.metalab.repository.AdministradorRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAdminService {

    private final AdministradorRepository administradores;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public UsuarioAdminService(
            AdministradorRepository administradores,
            PasswordEncoder encoder,
            Clock clock
    ) {
        this.administradores = administradores;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResposta> listar() {
        return administradores.findAllByOrderByNomeAsc()
                .stream()
                .map(UsuarioAdminResposta::de)
                .toList();
    }

    @Transactional
    public UsuarioAdminResposta criar(UsuarioCriacaoRequest req) {
        String email = normalizarEmail(req.email());

        if (administradores.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        Administrador usuario = new Administrador(
                req.nome().trim(),
                email,
                encoder.encode(req.senha()),
                req.perfil(),
                Instant.now(clock)
        );

        return UsuarioAdminResposta.de(administradores.save(usuario));
    }

    @Transactional
    public UsuarioAdminResposta atualizar(Long id, UsuarioAtualizacaoRequest req) {
        Administrador usuario = buscar(id);
        String email = normalizarEmail(req.email());

        if (administradores.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        if (usuario.getPerfil() == Perfil.ADMIN
                && usuario.isAtivo()
                && req.perfil() != Perfil.ADMIN) {
            protegerUltimoAdmin();
        }

        usuario.atualizarDados(req.nome().trim(), email, req.perfil());

        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.alterarSenha(encoder.encode(req.senha()));
        }

        return UsuarioAdminResposta.de(administradores.save(usuario));
    }

    @Transactional
    public UsuarioAdminResposta alterarStatus(Long id, boolean ativo) {
        Administrador usuario = buscar(id);

        if (usuario.isAtivo() && !ativo && usuario.getPerfil() == Perfil.ADMIN) {
            protegerUltimoAdmin();
        }

        usuario.alterarStatus(ativo);
        return UsuarioAdminResposta.de(administradores.save(usuario));
    }

    private Administrador buscar(Long id) {
        return administradores.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private void protegerUltimoAdmin() {
        if (administradores.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Não é possível remover ou rebaixar o último administrador ativo."
            );
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
