
package br.com.metalab.repository;

import br.com.metalab.model.Sorteio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SorteioRepository extends JpaRepository<Sorteio, Long> {

    List<Sorteio> findByCampanhaIdOrderByNumeroDesc(Long campanhaId);

    boolean existsByCampanhaIdAndNumero(Long campanhaId, Integer numero);

    Optional<Sorteio> findTopByCampanhaIdOrderByNumeroDesc(Long campanhaId);
}
