
package br.com.metalab.controller;

import jakarta.validation.constraints.NotNull;

public record StatusUsuarioRequest(@NotNull Boolean ativo) {}
