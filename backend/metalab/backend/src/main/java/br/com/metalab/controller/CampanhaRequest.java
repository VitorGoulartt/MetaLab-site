
package br.com.metalab.controller;

import br.com.metalab.model.CampanhaStatus;
import java.time.Instant;

public record CampanhaRequest(
    String nome,
    String slug,
    Instant inicioEm,
    Instant fimEm,
    CampanhaStatus status,
    String regulamentoUrl
) {}
