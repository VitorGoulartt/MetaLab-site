package br.com.metalab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "campanhas")
public class Campanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
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

    @Column(name = "versao_regulamento", nullable = false, length = 20)
    private String versaoRegulamento;

    @Column(name = "versao_privacidade", nullable = false, length = 20)
    private String versaoPrivacidade;

    protected Campanha() {
    }

    /** Recebe participações só se estiver ATIVA e dentro do período. */
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
    public String getVersaoRegulamento() { return versaoRegulamento; }
    public String getVersaoPrivacidade() { return versaoPrivacidade; }
}
