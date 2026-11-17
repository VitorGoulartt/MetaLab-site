
package br.com.metalab.controller;

import br.com.metalab.model.Ganhador;
import br.com.metalab.model.Sorteio;
import br.com.metalab.repository.GanhadorRepository;
import br.com.metalab.service.SorteioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sorteios")
public class SorteioController {

    private final SorteioService sorteios;
    private final GanhadorRepository ganhadores;

    public SorteioController(
        SorteioService sorteios,
        GanhadorRepository ganhadores
    ) {
        this.sorteios = sorteios;
        this.ganhadores = ganhadores;
    }

    @PostMapping
    public ResponseEntity<SorteioResposta> executar(
        @Valid @RequestBody SorteioRequest req,
        Principal principal
    ) {
        Sorteio sorteio = sorteios.executar(
            req.campanhaId(),
            req.premio(),
            req.quantidadeGanhadores(),
            principal.getName()
        );

        List<SorteioResposta.GanhadorResposta> resultado =
            ganhadores.buscarPorSorteio(sorteio.getId())
                .stream()
                .map(this::converterGanhador)
                .toList();

        SorteioResposta resposta = new SorteioResposta(
            sorteio.getId(),
            sorteio.getNumero(),
            sorteio.getPremio(),
            sorteio.getQuantidadeGanhadores(),
            sorteio.getRealizadoEm(),
            resultado
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    private SorteioResposta.GanhadorResposta converterGanhador(Ganhador ganhador) {
        String nome = ganhador.getParticipacao()
            .getCliente()
            .getNomeCompleto();

        return new SorteioResposta.GanhadorResposta(
            ganhador.getPosicao(),
            SorteioResposta.nomeExibicao(nome)
        );
    }
}
