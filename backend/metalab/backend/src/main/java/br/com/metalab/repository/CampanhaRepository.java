package br.com.metalab.repository;

import br.com.metalab.model.Campanha;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    Optional<Campanha> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Campanha c WHERE c.id = :id")
    Optional<Campanha> buscarPorIdComBloqueio(@Param("id") Long id);
    boolean existsBySlugAndIdNot(String slug, Long id);

}
