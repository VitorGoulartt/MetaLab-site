package br.com.metalab.controller;

import br.com.metalab.service.ApiException;
import br.com.metalab.service.ParticipacaoService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResposta> aoErroDaApi(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErroResposta(e.getMessage(), e.getCampos()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> aoDadosInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResposta("Confira os dados informados.", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> aoCorpoIlegivel(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErroResposta("Requisição inválida.", Map.of()));
    }

    /** Duas requisições do mesmo CPF ao mesmo tempo: a restrição única do banco barra a segunda. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> aoConflitoNoBanco(DataIntegrityViolationException e) {
        return aoErroDaApi(ParticipacaoService.jaParticipa());
    }
}
