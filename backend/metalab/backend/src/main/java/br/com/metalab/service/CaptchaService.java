package br.com.metalab.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Confere no servidor o token do Cloudflare Turnstile. Na dúvida (erro, rede), recusa. */
@Service
public class CaptchaService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);
    private static final String URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestClient http;
    private final String segredo;

    public CaptchaService(@Value("${metalab.captcha.secret}") String segredo) {
        this.segredo = segredo;
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        this.http = RestClient.builder().requestFactory(fabrica).build();
    }

    public boolean verificar(String token, String ipDoCliente) {
        if (token == null || token.isBlank() || token.length() > 2048) {
            return false;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", segredo);
        form.add("response", token);
        if (ipDoCliente != null && !ipDoCliente.isBlank()) {
            form.add("remoteip", ipDoCliente);
        }
        try {
            Map<String, Object> resposta = http.post()
                    .uri(URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() { });
            return resposta != null && Boolean.TRUE.equals(resposta.get("success"));
        } catch (Exception e) {
            log.warn("Falha ao consultar o Turnstile: {}", e.getMessage());
            return false;
        }
    }
}
