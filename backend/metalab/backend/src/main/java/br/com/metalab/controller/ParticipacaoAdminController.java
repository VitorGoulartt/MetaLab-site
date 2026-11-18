package br.com.metalab.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.metalab.repository.ParticipacaoRepository;

@RestController
@RequestMapping("/api/admin/campanhas")
public class ParticipacaoAdminController {

    private final ParticipacaoRepository participacoes;

    public ParticipacaoAdminController(ParticipacaoRepository participacoes) {
        this.participacoes = participacoes;
    }

    @GetMapping("/{campanhaId}/participantes-elegiveis")
    public List<ParticipanteElegivelResposta> listar(
        @PathVariable Long campanhaId
    ) {
        return participacoes.buscarElegiveis(campanhaId)
            .stream()
            .map(p -> new ParticipanteElegivelResposta(
                SorteioResposta.nomeExibicao(
                    p.getCliente().getNomeCompleto()
                )
            ))
            .toList();
    }
}