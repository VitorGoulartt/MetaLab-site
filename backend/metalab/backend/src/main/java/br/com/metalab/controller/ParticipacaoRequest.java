package br.com.metalab.controller;

import br.com.metalab.model.FaixaEtaria;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParticipacaoRequest(
        @NotBlank(message = "Informe o nome.") @Size(max = 150, message = "Nome muito longo.") String nome,
        @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.")
        @Size(max = 254, message = "E-mail muito longo.") String email,
        @NotBlank(message = "Informe o telefone.") @Size(max = 20, message = "Telefone inválido.") String telefone,
        @NotBlank(message = "Informe o CPF.") @Size(max = 14, message = "CPF inválido.") String cpf,
        @NotNull(message = "Informe a faixa etária.") FaixaEtaria faixaEtaria,
        @Size(max = 150, message = "Nome muito longo.") String responsavelNome,
        @AssertTrue(message = "É preciso aceitar o regulamento e a política de privacidade.") boolean aceite,
        @NotBlank(message = "Confirme que você não é um robô.") String captchaToken) {
}
