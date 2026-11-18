package br.com.metalab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;


@Entity
@Table(name = "participacoes")
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_etaria", nullable = false, length = 10)
    private FaixaEtaria faixaEtaria;

    @Column(name = "responsavel_nome", length = 150)
    private String responsavelNome;

    @Column(name = "consentimento_em", nullable = false)
    private Instant consentimentoEm;

    @Column(name = "versao_regulamento", nullable = false, length = 20)
    private String versaoRegulamento;

    @Column(name = "versao_privacidade", nullable = false, length = 20)
    private String versaoPrivacidade;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Participacao() {
    }

    public Participacao(Cliente cliente, Campanha campanha, FaixaEtaria faixaEtaria,
                        String responsavelNome, Instant agora) {
        this.cliente = cliente;
        this.campanha = campanha;
        this.faixaEtaria = faixaEtaria;
        this.responsavelNome = responsavelNome;
        this.consentimentoEm = agora;
        this.versaoRegulamento = campanha.getVersaoRegulamento();
        this.versaoPrivacidade = campanha.getVersaoPrivacidade();
        this.criadoEm = agora;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Campanha getCampanha() { return campanha; }
    public FaixaEtaria getFaixaEtaria() { return faixaEtaria; }
    public Instant getCriadoEm() { return criadoEm; }
}
