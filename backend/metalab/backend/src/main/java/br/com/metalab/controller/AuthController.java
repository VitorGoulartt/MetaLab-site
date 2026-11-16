package br.com.metalab.controller;

import br.com.metalab.model.Administrador;
import br.com.metalab.repository.AdministradorRepository;
import br.com.metalab.security.LimiteTentativas;
import br.com.metalab.service.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextoRepository;
    private final AdministradorRepository administradores;
    private final LimiteTentativas limite;
    private final Clock clock;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextoRepository,
                          AdministradorRepository administradores, LimiteTentativas limite, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.contextoRepository = contextoRepository;
        this.administradores = administradores;
        this.limite = limite;
        this.clock = clock;
    }

    @PostMapping("/login")
    public UsuarioResposta entrar(@Valid @RequestBody LoginRequest req, HttpServletRequest http,
                                  HttpServletResponse resposta) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        String chave = email + "|" + http.getRemoteAddr();
        limite.verificar(chave);

        Authentication autenticado;
        try {
            autenticado = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, req.senha()));
        } catch (AuthenticationException e) {
            limite.registrarFalha(chave);
            // A mesma mensagem para e-mail inexistente, senha errada e usuário desativado.
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.");
        }
        limite.limpar(chave);

        if (http.getSession(false) != null) {
            http.changeSessionId(); // evita fixação de sessão
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        contextoRepository.saveContext(contexto, http, resposta);

        Administrador admin = administradores.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));
        admin.registrarLogin(Instant.now(clock));
        administradores.save(admin);
        return UsuarioResposta.de(admin);
    }

    @GetMapping("/me")
    public UsuarioResposta eu(Principal principal) {
        return administradores.findByEmailIgnoreCase(principal.getName())
                .filter(Administrador::isAtivo)
                .map(UsuarioResposta::de)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest http) {
        HttpSession sessao = http.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
