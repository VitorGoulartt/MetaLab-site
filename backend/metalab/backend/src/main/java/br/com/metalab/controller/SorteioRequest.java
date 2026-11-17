
package br.com.metalab.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SorteioRequest(
    @NotNull(message = "Informe a campanha.")
    @Positive(message = "Campanha inválida.")
    Long campanhaId,

    @NotBlank(message = "Informe o prêmio.")
    @Size(max = 150, message = "Nome do prêmio muito longo.")
    String premio,

    @NotNull(message = "Informe a quantidade de ganhadores.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    Integer quantidadeGanhadores
) {
}
