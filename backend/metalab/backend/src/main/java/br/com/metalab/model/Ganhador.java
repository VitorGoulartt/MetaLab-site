package br.com.metalab.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "ganhadores",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_ganhadores_campanha_participacao",
            columnNames = {"campanha_id", "participacao_id"}
        ),
        @UniqueConstraint(
            name = "uq_ganhadores_sorteio_posicao",
            columnNames = {"sorteio_id", "posicao"}
        )
    }
)
public class Ganhador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sorteio_id", nullable = false)
    private Sorteio sorteio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participacao_id", nullable = false)
    private Participacao participacao;

    @Column(nullable = false)
    private Integer posicao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    protected Ganhador() {
    }

    public Ganhador(
        Sorteio sorteio,
        Campanha campanha,
        Participacao participacao,
        Integer posicao
    ) {
        this.sorteio = sorteio;
        this.campanha = campanha;
        this.participacao = participacao;
        this.posicao = posicao;
    }

    public Long getId() { return id; }
    public Sorteio getSorteio() { return sorteio; }
    public Campanha getCampanha() { return campanha; }
    public Participacao getParticipacao() { return participacao; }
    public Integer getPosicao() { return posicao; }
    public Instant getCriadoEm() { return criadoEm; }
}