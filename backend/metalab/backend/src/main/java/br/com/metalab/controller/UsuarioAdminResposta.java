
package br.com.metalab.controller;

import br.com.metalab.model.Administrador;
import java.time.Instant;

public record UsuarioAdminResposta(
        Long id,
        String nome,
        String email,
        String perfil,
        boolean ativo,
        Instant criadoEm,
        Instant ultimoLoginEm
) {
    public static UsuarioAdminResposta de(Administrador a) {
        return new UsuarioAdminResposta(
                a.getId(),
                a.getNome(),
                a.getEmail(),
                a.getPerfil().name(),
                a.isAtivo(),
                a.getCriadoEm(),
                a.getUltimoLoginEm()
        );
    }
}
