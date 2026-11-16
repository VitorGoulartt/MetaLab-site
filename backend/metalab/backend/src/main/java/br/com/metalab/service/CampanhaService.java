package br.com.metalab.service;

import br.com.metalab.controller.CampanhaResposta;
import br.com.metalab.model.Campanha;
import br.com.metalab.model.CampanhaStatus;
import br.com.metalab.repository.CampanhaRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampanhaService {

    private final CampanhaRepository campanhas;
    private final Clock clock;

    public CampanhaService(CampanhaRepository campanhas, Clock clock) {
        this.campanhas = campanhas;
        this.clock = clock;
    }

    /** Campanhas em RASCUNHO não aparecem: para o público, é como se não existissem. */
    @Transactional(readOnly = true)
    public CampanhaResposta buscarPublica(String slug) {
        Campanha campanha = campanhas.findBySlug(slug)
                .filter(c -> c.getStatus() != CampanhaStatus.RASCUNHO)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Campanha não encontrada."));
        return new CampanhaResposta(campanha.getNome(), campanha.estaAberta(Instant.now(clock)), campanha.getRegulamentoUrl());
    }
}
