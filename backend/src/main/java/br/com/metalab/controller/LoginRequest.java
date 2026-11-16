package br.com.metalab.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.")
        @Size(max = 254, message = "E-mail muito longo.") String email,
        @NotBlank(message = "Informe a senha.") @Size(max = 100, message = "Senha muito longa.") String senha) {
}
