
package br.com.metalab.controller;

import br.com.metalab.service.UsuarioAdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioAdminService service;

    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioAdminResposta> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResposta criar(@Valid @RequestBody UsuarioCriacaoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public UsuarioAdminResposta atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAtualizacaoRequest req
    ) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    public UsuarioAdminResposta alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUsuarioRequest req
    ) {
        return service.alterarStatus(id, req.ativo());
    }
}
