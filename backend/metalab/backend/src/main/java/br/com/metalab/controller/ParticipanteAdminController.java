package br.com.metalab.controller;

import br.com.metalab.repository.ParticipacaoRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/participantes")
public class ParticipanteAdminController {

    private final ParticipacaoRepository participacoes;

    public ParticipanteAdminController(ParticipacaoRepository participacoes) {
        this.participacoes = participacoes;
    }

    @GetMapping
    public List<ParticipanteAdminResposta> listar(
        @RequestParam(required = false) Long campanhaId
    ) {
        return participacoes.listarTodas(campanhaId)
            .stream()
            .map(ParticipanteAdminResposta::de)
            .toList();
    }
}
