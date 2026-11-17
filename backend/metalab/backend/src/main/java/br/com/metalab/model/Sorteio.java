package br.com.metalab.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "sorteios",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_sorteios_campanha_numero",
            columnNames = {"campanha_id", "numero"}
        )
    }
)
public class Sorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 150)
    private String premio;

    @Column(name = "quantidade_ganhadores", nullable = false)
    private Integer quantidadeGanhadores;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private SorteioStatus status = SorteioStatus.PENDENTE;

    @Column(name = "realizado_em")
    private Instant realizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realizado_por")
    private Administrador realizadoPor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "elegiveis_snapshot", columnDefinition = "jsonb")
    private List<Long> elegiveisSnapshot;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    protected Sorteio() {
    }

    public Sorteio(
        Campanha campanha,
        Integer numero,
        String premio,
        Integer quantidadeGanhadores
    ) {
        this.campanha = campanha;
        this.numero = numero;
        this.premio = premio;
        this.quantidadeGanhadores = quantidadeGanhadores;
        this.status = SorteioStatus.PENDENTE;
    }

    public void finalizar(
        Administrador administrador,
        Instant realizadoEm,
        List<Long> elegiveis
    ) {
        this.realizadoPor = administrador;
        this.realizadoEm = realizadoEm;
        this.elegiveisSnapshot = List.copyOf(elegiveis);
        this.status = SorteioStatus.REALIZADO;
    }

    public Long getId() { return id; }
    public Campanha getCampanha() { return campanha; }
    public Integer getNumero() { return numero; }
    public String getPremio() { return premio; }
    public Integer getQuantidadeGanhadores() { return quantidadeGanhadores; }
    public SorteioStatus getStatus() { return status; }
    public Instant getRealizadoEm() { return realizadoEm; }
    public Administrador getRealizadoPor() { return realizadoPor; }
    public List<Long> getElegiveisSnapshot() { return elegiveisSnapshot; }
    public Instant getCriadoEm() { return criadoEm; }
}