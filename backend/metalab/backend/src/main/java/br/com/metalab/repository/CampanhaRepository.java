package br.com.metalab.repository;

import br.com.metalab.model.Campanha;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    Optional<Campanha> findBySlug(String slug);
}
