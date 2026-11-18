
package br.com.metalab.controller;

import br.com.metalab.service.CampanhaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/campanhas")
public class CampanhaAdminController {

    private final CampanhaService campanhas;

    public CampanhaAdminController(CampanhaService campanhas) {
        this.campanhas = campanhas;
    }

    @GetMapping
    public List<CampanhaAdminResposta> listar() {
        return campanhas.listarAdministrativas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampanhaAdminResposta criar(@RequestBody CampanhaRequest dados) {
        return campanhas.criar(dados);
    }

    @PutMapping("/{id}")
    public CampanhaAdminResposta editar(
            @PathVariable Long id,
            @RequestBody CampanhaRequest dados) {
        return campanhas.editar(id, dados);
    }
}
