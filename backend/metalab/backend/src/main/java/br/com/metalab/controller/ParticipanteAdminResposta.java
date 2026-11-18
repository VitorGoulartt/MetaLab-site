
package br.com.metalab.controller;

import br.com.metalab.model.Participacao;
import java.time.Instant;

public record ParticipanteAdminResposta(
    Long id,
    String nome,
    String email,
    String telefone,
    String cpfMascarado,
    Long campanhaId,
    String campanhaNome,
    Instant criadoEm
) {
    public static ParticipanteAdminResposta de(Participacao p) {
        return new ParticipanteAdminResposta(
            p.getId(),
            p.getCliente().getNomeCompleto(),
            p.getCliente().getEmail(),
            p.getCliente().getTelefone(),
            p.getCliente().getCpfMascarado(),
            p.getCampanha().getId(),
            p.getCampanha().getNome(),
            p.getCriadoEm()
            
        );
    }
}
