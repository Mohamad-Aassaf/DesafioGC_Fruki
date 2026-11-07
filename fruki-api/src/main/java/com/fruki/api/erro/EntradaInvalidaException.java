package com.fruki.api.erro;

import java.util.List;

/** Dado de entrada que não passou na validação. Vira HTTP 400 com a lista de problemas. */
public class EntradaInvalidaException extends RuntimeException {
    private final List<String> detalhes;

    public EntradaInvalidaException(String mensagem, List<String> detalhes) {
        super(mensagem);
        this.detalhes = List.copyOf(detalhes);
    }

    public EntradaInvalidaException(String detalhe) {
        this("Dados inválidos", List.of(detalhe));
    }

    public List<String> getDetalhes() {
        return detalhes;
    }
}
