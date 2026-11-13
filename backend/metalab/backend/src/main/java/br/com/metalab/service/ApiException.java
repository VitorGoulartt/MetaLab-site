package br.com.metalab.service;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Erro esperado da API: vira uma resposta JSON com o status e a mensagem indicados. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final Map<String, String> campos;

    public ApiException(HttpStatus status, String mensagem, Map<String, String> campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos;
    }

    public ApiException(HttpStatus status, String mensagem) {
        this(status, mensagem, Map.of());
    }

    public HttpStatus getStatus() { return status; }
    public Map<String, String> getCampos() { return campos; }
}
