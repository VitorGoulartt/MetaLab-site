package br.com.metalab.service;

import br.com.metalab.controller.ParticipacaoRequest;
import br.com.metalab.model.Campanha;
import br.com.metalab.model.Cliente;
import br.com.metalab.model.FaixaEtaria;
import br.com.metalab.model.OrigemCadastro;
import br.com.metalab.model.Participacao;
import br.com.metalab.repository.CampanhaRepository;
import br.com.metalab.repository.ClienteRepository;
import br.com.metalab.repository.ParticipacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipacaoService {

    private final CampanhaRepository campanhas;
    private final ClienteRepository clientes;
    private final ParticipacaoRepository participacoes;
    private final CpfService cpfService;
    private final Clock clock;

    public ParticipacaoService(CampanhaRepository campanhas, ClienteRepository clientes,
                               ParticipacaoRepository participacoes, CpfService cpfService, Clock clock) {
        this.campanhas = campanhas;
        this.clientes = clientes;
        this.participacoes = participacoes;
        this.cpfService = cpfService;
        this.clock = clock;
    }

    @Transactional
    public void registrar(String slug, ParticipacaoRequest req) {
        Instant agora = Instant.now(clock);

        Campanha campanha = campanhas.findBySlug(slug)
                .filter(c -> c.estaAberta(agora))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Esta campanha não está recebendo participações."));

        validar(req);

        String cpf = cpfService.normalizar(req.cpf());
        String hash = cpfService.hash(cpf);

        // Se o CPF já existe, os dados dele NÃO são sobrescritos: quem digitar o CPF de
        // outra pessoa não consegue alterar o e-mail ou o telefone dela.
        Cliente cliente = clientes.findByCpfHash(hash).orElseGet(() -> clientes.save(new Cliente(
                req.nome().trim(),
                req.email().trim().toLowerCase(Locale.ROOT),
                req.telefone().replaceAll("\\D", ""),
                hash,
                cpfService.cifrar(cpf),
                cpfService.mascarar(cpf),
                OrigemCadastro.QR_CODE,
                agora)));

        if (participacoes.existsByClienteIdAndCampanhaId(cliente.getId(), campanha.getId())) {
            throw jaParticipa();
        }

        String responsavel = req.faixaEtaria() == FaixaEtaria.MENOR ? req.responsavelNome().trim() : null;
        participacoes.saveAndFlush(new Participacao(cliente, campanha, req.faixaEtaria(), responsavel, agora));
    }

    public static ApiException jaParticipa() {
        return new ApiException(HttpStatus.CONFLICT, "Este CPF já está participando desta campanha.");
    }

    private void validar(ParticipacaoRequest req) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (!doisNomes(req.nome())) {
            erros.put("nome", "Informe nome e sobrenome.");
        }
        int digitosTelefone = req.telefone().replaceAll("\\D", "").length();
        if (digitosTelefone != 10 && digitosTelefone != 11) {
            erros.put("telefone", "Digite o telefone com DDD, com 10 ou 11 números.");
        }
        if (!cpfService.valido(req.cpf())) {
            erros.put("cpf", "Digite um CPF válido.");
        }
        if (req.faixaEtaria() == FaixaEtaria.MENOR && !doisNomes(req.responsavelNome())) {
            erros.put("responsavelNome", "Informe nome e sobrenome do responsável.");
        }
        if (!erros.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Confira os dados informados.", erros);
        }
    }

    private boolean doisNomes(String texto) {
        return texto != null && texto.trim().split("\\s+").length >= 2;
    }
}
