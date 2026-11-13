package br.com.metalab.controller;

import java.util.Map;

public record ErroResposta(String mensagem, Map<String, String> campos) {
}
