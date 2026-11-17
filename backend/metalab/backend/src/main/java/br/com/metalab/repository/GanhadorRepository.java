
package br.com.metalab.repository;

import br.com.metalab.model.Ganhador;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GanhadorRepository extends JpaRepository<Ganhador, Long> {

    @Query("""
        SELECT g FROM Ganhador g
        JOIN FETCH g.participacao p
        JOIN FETCH p.cliente
        WHERE g.sorteio.id = :sorteioId
        ORDER BY g.posicao ASC
    """)
    List<Ganhador> buscarPorSorteio(
        @Param("sorteioId") Long sorteioId
    );
}
