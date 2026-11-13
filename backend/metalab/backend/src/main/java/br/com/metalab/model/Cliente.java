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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    // Coluna CHAR(64) no banco: o JdbcTypeCode faz o Hibernate aceitar o tipo na validação.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf_hash", nullable = false, length = 64)
    private String cpfHash;

    @Column(name = "cpf_cifrado", nullable = false, length = 500)
    private String cpfCifrado;

    @Column(name = "cpf_mascarado", nullable = false, length = 14)
    private String cpfMascarado;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_cadastro", nullable = false, length = 20)
    private OrigemCadastro origemCadastro;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Cliente() {
    }

    public Cliente(String nomeCompleto, String email, String telefone, String cpfHash,
                   String cpfCifrado, String cpfMascarado, OrigemCadastro origemCadastro, Instant criadoEm) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.telefone = telefone;
        this.cpfHash = cpfHash;
        this.cpfCifrado = cpfCifrado;
        this.cpfMascarado = cpfMascarado;
        this.origemCadastro = origemCadastro;
        this.criadoEm = criadoEm;
    }

    public Long getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getCpfMascarado() { return cpfMascarado; }
    public OrigemCadastro getOrigemCadastro() { return origemCadastro; }
}
