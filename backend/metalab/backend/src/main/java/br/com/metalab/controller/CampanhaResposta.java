package br.com.metalab.controller;

/** O que a página pública precisa saber de uma campanha. */
public record CampanhaResposta(String nome, boolean aberta, String regulamentoUrl) {
}
