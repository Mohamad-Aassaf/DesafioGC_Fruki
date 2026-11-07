package com.fruki.api.clima;

import java.util.List;

/** Fonte de previsão do tempo. Pode lançar exceção (rede, timeout, resposta inválida). */
public interface ProvedorClima {
    String nome();

    List<DiaClima> previsao(String uf) throws Exception;
}
