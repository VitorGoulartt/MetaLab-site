
package br.com.metalab.repository;

import br.com.metalab.model.Participacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

    boolean existsByClienteIdAndCampanhaId(Long clienteId, Long campanhaId);


    @Query("""
        SELECT p FROM Participacao p
        JOIN FETCH p.cliente
        JOIN FETCH p.campanha
        WHERE (:campanhaId IS NULL OR p.campanha.id = :campanhaId)
        ORDER BY p.criadoEm DESC
    """)
    List<Participacao> listarTodas(
        @Param("campanhaId") Long campanhaId
    );


    @Query("""
        SELECT p FROM Participacao p
        JOIN FETCH p.cliente
        WHERE p.campanha.id = :campanhaId
        AND NOT EXISTS (
            SELECT g.id FROM Ganhador g
            WHERE g.campanha.id = :campanhaId
            AND g.participacao.id = p.id
        )
    """)
    List<Participacao> buscarElegiveis(
        @Param("campanhaId") Long campanhaId
    );
}
