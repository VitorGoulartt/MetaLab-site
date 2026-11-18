package br.com.metalab.controller;

import br.com.metalab.model.Campanha;
import java.time.Instant;

public record CampanhaAdminResposta(
    Long id,
    String nome,
    String slug,
    String status,
    Instant inicioEm,
    Instant fimEm
) {
    public static CampanhaAdminResposta de(Campanha campanha) {
        return new CampanhaAdminResposta(
            campanha.getId(),
            campanha.getNome(),
            campanha.getSlug(),
            campanha.getStatus().name(),
            campanha.getInicioEm(),
            campanha.getFimEm()
        );
    }
}
