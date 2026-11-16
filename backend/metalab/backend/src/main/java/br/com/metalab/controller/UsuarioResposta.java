package br.com.metalab.controller;

import br.com.metalab.model.Administrador;

public record UsuarioResposta(Long id, String nome, String email, String perfil) {

    public static UsuarioResposta de(Administrador a) {
        return new UsuarioResposta(a.getId(), a.getNome(), a.getEmail(), a.getPerfil().name());
    }
}
