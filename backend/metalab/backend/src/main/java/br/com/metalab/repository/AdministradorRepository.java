package br.com.metalab.repository;

import br.com.metalab.model.Administrador;
import br.com.metalab.model.Perfil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);
}
