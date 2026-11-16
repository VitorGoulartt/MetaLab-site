package br.com.metalab.controller;

import br.com.metalab.service.CampanhaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    private final CampanhaService campanhas;

    public CampanhaController(CampanhaService campanhas) {
        this.campanhas = campanhas;
    }

    @GetMapping("/{slug}")
    public CampanhaResposta buscar(@PathVariable String slug) {
        return campanhas.buscarPublica(slug);
    }
}
