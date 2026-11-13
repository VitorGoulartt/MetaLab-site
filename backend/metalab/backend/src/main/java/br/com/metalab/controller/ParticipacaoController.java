package br.com.metalab.controller;

import br.com.metalab.service.ApiException;
import br.com.metalab.service.CaptchaService;
import br.com.metalab.service.ParticipacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campanhas")
public class ParticipacaoController {

    private final CaptchaService captcha;
    private final ParticipacaoService participacoes;

    public ParticipacaoController(CaptchaService captcha, ParticipacaoService participacoes) {
        this.captcha = captcha;
        this.participacoes = participacoes;
    }

    @PostMapping("/{slug}/participacoes")
    public ResponseEntity<ParticipacaoResposta> participar(@PathVariable String slug,
                                                           @Valid @RequestBody ParticipacaoRequest req,
                                                           HttpServletRequest http) {
        // O CAPTCHA vem antes de qualquer consulta ao banco. Cada token vale uma vez só.
        if (!captcha.verificar(req.captchaToken(), http.getRemoteAddr())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Não foi possível confirmar que você não é um robô. Tente de novo.");
        }
        participacoes.registrar(slug, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ParticipacaoResposta("Participação confirmada."));
    }
}
