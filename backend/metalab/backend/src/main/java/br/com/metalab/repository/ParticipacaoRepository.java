package br.com.metalab.repository;

import br.com.metalab.model.Participacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {
    boolean existsByClienteIdAndCampanhaId(Long clienteId, Long campanhaId);
}
