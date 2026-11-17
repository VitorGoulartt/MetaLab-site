
package br.com.metalab.controller;

import br.com.metalab.model.Sorteio;
import java.time.Instant;
import java.util.List;

public record SorteioResposta(
    Long id,
    Integer numero,
    String premio,
    Integer quantidadeGanhadores,
    Instant realizadoEm,
    List<GanhadorResposta> ganhadores
) {
    public record GanhadorResposta(
        Integer posicao,
        String nomeExibicao
    ) {}

    public static String nomeExibicao(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            return "Participante";
        }

        String[] partes = nomeCompleto.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0];
        }

        String ultimo = partes[partes.length - 1];

        return partes[0] + " " + ultimo.charAt(0) + ".";
    }
}
