
package br.com.metalab.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "campanhas")
public class Campanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String slug;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "inicio_em", nullable = false)
    private Instant inicioEm;

    @Column(name = "fim_em", nullable = false)
    private Instant fimEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CampanhaStatus status;

    @Column(name = "regulamento_url", length = 500)
    private String regulamentoUrl;

    @Column(name = "versao_regulamento", nullable = false, length = 20)
    private String versaoRegulamento;

    @Column(name = "versao_privacidade", nullable = false, length = 20)
    private String versaoPrivacidade;

    public Campanha() {
    }

    public void atualizar(
            String nome,
            String slug,
            Instant inicioEm,
            Instant fimEm,
            CampanhaStatus status,
            String regulamentoUrl,
            String versaoRegulamento,
            String versaoPrivacidade) {
        this.nome = nome;
        this.slug = slug;
        this.inicioEm = inicioEm;
        this.fimEm = fimEm;
        this.status = status;
        this.regulamentoUrl = regulamentoUrl;
        this.versaoRegulamento = versaoRegulamento;
        this.versaoPrivacidade = versaoPrivacidade;
    }

    public boolean estaAberta(Instant agora) {
        return status == CampanhaStatus.ATIVA
                && !agora.isBefore(inicioEm)
                && agora.isBefore(fimEm);
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getNome() { return nome; }
    public Instant getInicioEm() { return inicioEm; }
    public Instant getFimEm() { return fimEm; }
    public CampanhaStatus getStatus() { return status; }
    public String getRegulamentoUrl() { return regulamentoUrl; }
    public String getVersaoRegulamento() { return versaoRegulamento; }
    public String getVersaoPrivacidade() { return versaoPrivacidade; }
}
