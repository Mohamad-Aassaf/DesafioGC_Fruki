package com.fruki.api.erro;

/** Recurso inexistente (cliente, produto...). Vira HTTP 404. */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
