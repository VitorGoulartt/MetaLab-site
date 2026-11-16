package br.com.metalab.security;

import br.com.metalab.service.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Trava o login depois de 5 erros em 15 minutos (por e-mail + IP). Fica na memória: reiniciar o servidor zera. */
@Component
public class LimiteTentativas {

    private static final int MAXIMO = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private record Registro(int falhas, Instant inicio) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Clock clock;

    public LimiteTentativas(Clock clock) {
        this.clock = clock;
    }

    public void verificar(String chave) {
        Registro r = registros.get(chave);
        if (r != null && !expirou(r) && r.falhas() >= MAXIMO) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas. Aguarde alguns minutos e tente de novo.");
        }
    }

    public void registrarFalha(String chave) {
        if (registros.size() > 10_000) {
            registros.values().removeIf(this::expirou);
        }
        Instant agora = Instant.now(clock);
        registros.compute(chave, (k, atual) ->
                (atual == null || expirou(atual)) ? new Registro(1, agora) : new Registro(atual.falhas() + 1, atual.inicio()));
    }

    public void limpar(String chave) {
        registros.remove(chave);
    }

    private boolean expirou(Registro r) {
        return Instant.now(clock).isAfter(r.inicio().plus(JANELA));
    }
}
