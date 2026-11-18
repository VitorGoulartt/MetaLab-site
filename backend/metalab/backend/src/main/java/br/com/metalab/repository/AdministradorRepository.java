package br.com.metalab.repository;

import br.com.metalab.model.Administrador;
import br.com.metalab.model.Perfil;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
     Optional<Administrador> findByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByPerfilAndAtivoTrue(Perfil perfil);

    List<Administrador> findAllByOrderByNomeAsc();
}
