package com.fruki.api.web;

import com.fruki.api.erro.ConflitoException;
import com.fruki.api.erro.EntradaInvalidaException;
import com.fruki.api.erro.NaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Todos os erros da API saem no mesmo formato:
 * {"status": 400, "erro": "Requisição inválida", "mensagem": "...", "detalhes": [...], "caminho": "/api/..."}
 * Erros inesperados viram 500 sem expor detalhes internos (o detalhe fica no log).
 */
@RestControllerAdvice
public class ErroHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

    public record Erro(LocalDateTime momento, int status, String erro, String mensagem, List<String> detalhes,
                       String caminho) {}

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<Erro> naoEncontrado(NaoEncontradoException e, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), List.of(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<Erro> conflito(ConflitoException e, HttpServletRequest req) {
        return erro(HttpStatus.CONFLICT, e.getMessage(), List.of(), req);
    }

    @ExceptionHandler(EntradaInvalidaException.class)
    ResponseEntity<Erro> entradaInvalida(EntradaInvalidaException e, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage(), e.getDetalhes(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Erro> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<String> detalhes = e.getBindingResult().getAllErrors().stream()
                .map(x -> {
                    if (x instanceof FieldError f && f.isBindingFailure()) {
                        return f.getField() + " com valor inválido: " + f.getRejectedValue();
                    }
                    return x.getDefaultMessage() == null ? x.toString() : x.getDefaultMessage();
                })
                .sorted().toList();
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", detalhes, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Erro> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, "JSON inválido ou campo com tipo errado (ex.: texto onde era número, data fora do formato AAAA-MM-DD)",
                List.of(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Erro> tipoErrado(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, "Parâmetro '" + e.getName() + "' com valor inválido: " + e.getValue(),
                List.of(), req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Erro> inesperado(Exception e, HttpServletRequest req) {
        // Erros do próprio Spring MVC (rota inexistente, método não permitido...) já trazem o status certo.
        if (e instanceof ErrorResponse er) {
            HttpStatusCode s = er.getStatusCode();
            String msg = er.getBody().getDetail() != null ? er.getBody().getDetail() : e.getMessage();
            return erro(HttpStatus.valueOf(s.value()), msg, List.of(), req);
        }
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente de novo; se continuar, veja o log da API.",
                List.of(), req);
    }

    private static ResponseEntity<Erro> erro(HttpStatus s, String mensagem, List<String> detalhes, HttpServletRequest req) {
        return ResponseEntity.status(s).body(new Erro(LocalDateTime.now(), s.value(), s.getReasonPhrase(), mensagem,
                detalhes, req.getRequestURI()));
    }
}
