package br.com.metalab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class CpfServiceTest {

    private final CpfService svc = new CpfService("x".repeat(32), Base64.getEncoder().encodeToString(new byte[32]));

    @Test
    void aceitaCpfValidoComOuSemFormatacao() {
        assertTrue(svc.valido("529.982.247-25"));
        assertTrue(svc.valido("52998224725"));
    }

    @Test
    void rejeitaDigitoVerificadorErrado() {
        assertFalse(svc.valido("529.982.247-26"));
    }

    @Test
    void rejeitaDigitosRepetidosETamanhoErrado() {
        assertFalse(svc.valido("111.111.111-11"));
        assertFalse(svc.valido("123"));
        assertFalse(svc.valido(null));
    }

    @Test
    void hashIgnoraFormatacaoETem64Caracteres() {
        assertEquals(svc.hash("529.982.247-25"), svc.hash("52998224725"));
        assertEquals(64, svc.hash("52998224725").length());
        assertNotEquals(svc.hash("52998224725"), svc.hash("11144477735"));
    }

    @Test
    void cifraEDecifraSemRepetirOTextoCifrado() {
        String a = svc.cifrar("52998224725");
        String b = svc.cifrar("52998224725");
        assertNotEquals("52998224725", a);
        assertNotEquals(a, b);
        assertEquals("52998224725", svc.decifrar(a));
    }

    @Test
    void mascaraEscondeOsPrimeirosETodosOsUltimosDigitos() {
        assertEquals("***.982.247-**", svc.mascarar("529.982.247-25"));
    }

    @Test
    void recusaSegredosFracos() {
        assertThrows(IllegalStateException.class, () -> new CpfService("curto", Base64.getEncoder().encodeToString(new byte[32])));
        assertThrows(IllegalStateException.class, () -> new CpfService("x".repeat(32), Base64.getEncoder().encodeToString(new byte[16])));
    }
}
