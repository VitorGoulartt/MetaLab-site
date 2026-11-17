
package br.com.metalab.service;

import br.com.metalab.model.*;
import br.com.metalab.repository.*;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SorteioService {

    private final CampanhaRepository campanhaRepository;
    private final AdministradorRepository administradorRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final SorteioRepository sorteioRepository;
    private final GanhadorRepository ganhadorRepository;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public SorteioService(
        CampanhaRepository campanhaRepository,
        AdministradorRepository administradorRepository,
        ParticipacaoRepository participacaoRepository,
        SorteioRepository sorteioRepository,
        GanhadorRepository ganhadorRepository,
        Clock clock
    ) {
        this.campanhaRepository = campanhaRepository;
        this.administradorRepository = administradorRepository;
        this.participacaoRepository = participacaoRepository;
        this.sorteioRepository = sorteioRepository;
        this.ganhadorRepository = ganhadorRepository;
        this.clock = clock;
    }

    @Transactional
    public Sorteio executar(
        Long campanhaId,
        String premio,
        Integer quantidadeGanhadores,
        String emailAdministrador
    ) {
        if (premio == null || premio.isBlank() || premio.length() > 150) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Informe um prêmio válido."
            );
        }

        if (quantidadeGanhadores == null || quantidadeGanhadores <= 0) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "A quantidade de ganhadores deve ser maior que zero."
            );
        }

        Campanha campanha = campanhaRepository
            .buscarPorIdComBloqueio(campanhaId)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Campanha não encontrada."
            ));

        Administrador administrador = administradorRepository
            .findByEmailIgnoreCase(emailAdministrador)
            .filter(Administrador::isAtivo)
            .orElseThrow(() -> new ApiException(
                HttpStatus.FORBIDDEN,
                "Administrador não encontrado ou inativo."
            ));

        List<Participacao> elegiveis =
            participacaoRepository.buscarElegiveis(campanha.getId());

        if (elegiveis.size() < quantidadeGanhadores) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Não existem participantes elegíveis suficientes."
            );
        }

        List<Long> snapshot = elegiveis.stream()
            .map(Participacao::getId)
            .toList();

        Sorteio sorteio = new Sorteio(
            campanha,
            proximoNumero(campanha.getId()),
            premio.trim(),
            quantidadeGanhadores
        );

        sorteio = sorteioRepository.save(sorteio);

        List<Participacao> selecionados = new ArrayList<>(elegiveis);
        Collections.shuffle(selecionados, random);

        List<Ganhador> ganhadores = new ArrayList<>();

        for (int i = 0; i < quantidadeGanhadores; i++) {
            ganhadores.add(new Ganhador(
                sorteio,
                campanha,
                selecionados.get(i),
                i + 1
            ));
        }

        ganhadorRepository.saveAll(ganhadores);

        sorteio.finalizar(
            administrador,
            clock.instant(),
            snapshot
        );

        return sorteioRepository.save(sorteio);
    }

    private Integer proximoNumero(Long campanhaId) {
        return sorteioRepository
            .findTopByCampanhaIdOrderByNumeroDesc(campanhaId)
            .map(sorteio -> sorteio.getNumero() + 1)
            .orElse(1);
    }
}
