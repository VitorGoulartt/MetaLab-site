package br.com.metalab.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Tudo sobre o CPF: validação, hash para busca e unicidade, cifra para o admin
 * ver o número e máscara para as telas de consulta. O CPF nunca vai em texto puro ao banco.
 */
@Service
public class CpfService {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecureRandom random = new SecureRandom();
    private final SecretKeySpec chaveAes;
    private final SecretKeySpec chaveHmac;

    public CpfService(@Value("${metalab.cpf.pepper}") String pepper,
                      @Value("${metalab.cpf.chave}") String chaveBase64) {
        if (pepper == null || pepper.length() < 32) {
            throw new IllegalStateException("CPF_PEPPER precisa ter pelo menos 32 caracteres (use: openssl rand -base64 32).");
        }
        byte[] chave;
        try {
            chave = Base64.getDecoder().decode(chaveBase64 == null ? "" : chaveBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("CPF_CHAVE precisa estar em Base64 (use: openssl rand -base64 32).", e);
        }
        if (chave.length != 32) {
            throw new IllegalStateException("CPF_CHAVE precisa ter 32 bytes (use: openssl rand -base64 32).");
        }
        this.chaveAes = new SecretKeySpec(chave, "AES");
        this.chaveHmac = new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String normalizar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public boolean valido(String cpf) {
        String d = normalizar(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        for (int t = 9; t < 11; t++) {
            int soma = 0;
            for (int i = 0; i < t; i++) {
                soma += (d.charAt(i) - '0') * (t + 1 - i);
            }
            int digito = (soma * 10) % 11 % 10;
            if (digito != d.charAt(t) - '0') {
                return false;
            }
        }
        return true;
    }

    /** HMAC-SHA256 em hexadecimal (64 caracteres): igual para o mesmo CPF, impossível de reverter sem o segredo. */
    public String hash(String cpf) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(chaveHmac);
            byte[] resultado = mac.doFinal(normalizar(cpf).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resultado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao calcular o hash do CPF.", e);
        }
    }

    /** AES-256-GCM com IV aleatório; o resultado é Base64 de (IV + texto cifrado). */
    public String cifrar(String cpf) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.ENCRYPT_MODE, chaveAes, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cifra.doFinal(normalizar(cpf).getBytes(StandardCharsets.UTF_8));
            byte[] saida = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, saida, 0, iv.length);
            System.arraycopy(cifrado, 0, saida, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(saida);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao cifrar o CPF.", e);
        }
    }

    /** Só o perfil ADMIN deve chegar aqui (a regra de acesso virá no painel). */
    public String decifrar(String cifrado) {
        try {
            byte[] entrada = Base64.getDecoder().decode(cifrado);
            byte[] iv = Arrays.copyOfRange(entrada, 0, IV_BYTES);
            byte[] dados = Arrays.copyOfRange(entrada, IV_BYTES, entrada.length);
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.DECRYPT_MODE, chaveAes, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cifra.doFinal(dados), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao decifrar o CPF.", e);
        }
    }

    /** 52998224725 vira ***.982.247-** */
    public String mascarar(String cpf) {
        String d = normalizar(cpf);
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }
}
