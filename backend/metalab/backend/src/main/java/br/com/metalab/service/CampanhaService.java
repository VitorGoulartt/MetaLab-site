
package br.com.metalab.service;

import br.com.metalab.controller.*;
import br.com.metalab.model.*;
import br.com.metalab.repository.CampanhaRepository;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
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

    @Transactional(readOnly = true)
    public List<CampanhaAdminResposta> listarAdministrativas() {
        return campanhas.findAll()
                .stream()
                .map(CampanhaAdminResposta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampanhaResposta buscarPublica(String slug) {
        Campanha campanha = campanhas.findBySlug(slug)
                .filter(c -> c.getStatus() != CampanhaStatus.RASCUNHO)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "Campanha não encontrada."));

        return new CampanhaResposta(
                campanha.getNome(),
                campanha.estaAberta(Instant.now(clock)),
                campanha.getRegulamentoUrl());
    }

    @Transactional
    public CampanhaAdminResposta criar(CampanhaRequest dados) {
        validar(dados, null);

        Campanha campanha = new Campanha();
        preencher(campanha, dados);

        return CampanhaAdminResposta.de(campanhas.save(campanha));
    }

    @Transactional
    public CampanhaAdminResposta editar(Long id, CampanhaRequest dados) {
        Campanha campanha = campanhas.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "Campanha não encontrada."));

        validar(dados, id);
        preencher(campanha, dados);

        return CampanhaAdminResposta.de(campanhas.save(campanha));
    }

    private void validar(CampanhaRequest dados, Long id) {
        if (dados.nome() == null || dados.nome().isBlank()
                || dados.nome().length() > 150) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Informe um nome de até 150 caracteres.");
        }

        String slug = normalizarSlug(dados.slug());

        if (slug.isBlank() || slug.length() > 80) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Informe um endereço válido de até 80 caracteres.");
        }

        if (dados.inicioEm() == null || dados.fimEm() == null
                || !dados.fimEm().isAfter(dados.inicioEm())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "A data final deve ser posterior à data inicial.");
        }

        if (dados.status() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Selecione o status da campanha.");
        }

        if (id == null) {
            if (campanhas.findBySlug(slug).isPresent()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Esse endereço já está sendo usado por outra campanha.");
            }
        } else if (campanhas.existsBySlugAndIdNot(slug, id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esse endereço já está sendo usado por outra campanha.");
        }

        if (dados.regulamentoUrl() != null
                && dados.regulamentoUrl().length() > 500) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "O endereço do regulamento é muito longo.");
        }
    }

    private void preencher(Campanha campanha, CampanhaRequest dados) {
        campanha.atualizar(
                dados.nome().trim(),
                normalizarSlug(dados.slug()),
                dados.inicioEm(),
                dados.fimEm(),
                dados.status(),
                dados.regulamentoUrl() == null || dados.regulamentoUrl().isBlank()
                        ? null : dados.regulamentoUrl().trim(),
                campanha.getVersaoRegulamento() == null ? "1.0"
                        : campanha.getVersaoRegulamento(),
                campanha.getVersaoPrivacidade() == null ? "1.0"
                        : campanha.getVersaoPrivacidade()
        );
    }

    private String normalizarSlug(String valor) {
        if (valor == null) return "";

        return Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT),
                        Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
