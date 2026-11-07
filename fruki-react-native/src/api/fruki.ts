// Cliente da API Java (fruki-api). Se a API não responder, o app usa o motor local.
import { useEffect, useRef, useState } from 'react';
import { Platform } from 'react-native';

import { Cliente } from '../data/dados';
import { Criterio, Parametros, Recomendacao } from '../motor/motor';

/**
 * Endereço da API. O emulador Android enxerga o computador como 10.0.2.2.
 * Num celular de verdade, troque pelo IP do computador na rede (ex.: http://192.168.0.10:8080).
 */
export const API_URL = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';

/** Quantos itens pedir: o app mostra 3, o resto serve para "trocar sugestão". */
const ITENS_POR_CLIENTE = 12;

type ClienteApi = {
  id: number;
  nome: string;
  cidade: string;
  uf: string;
  segmento: string;
  vendedor: string;
  produtosComprados: number[];
};

type ItemApi = {
  posicao: number;
  produtoId: number;
  produto: string;
  familia: string;
  preco: number;
  pontos: number;
  partes: Partial<Record<Criterio, number>>;
  motivos: string[];
};

type RespostaApi = { versao: string; recomendacoes: ItemApi[]; avisos: string[]; contexto: { fonteClima: string } };

export type RankingApi = { rk: Recomendacao[]; avisos: string[] };

async function getJson<T>(caminho: string, timeoutMs = 4000): Promise<T> {
  const controle = new AbortController();
  const timer = setTimeout(() => controle.abort(), timeoutMs);
  try {
    const r = await fetch(API_URL + caminho, { signal: controle.signal });
    if (!r.ok) throw new Error(`HTTP ${r.status} em ${caminho}`);
    return (await r.json()) as T;
  } finally {
    clearTimeout(timer);
  }
}

async function carregarVendedores(): Promise<Record<string, Cliente[]>> {
  const grupos = await getJson<{ vendedor: string; clientes: ClienteApi[] }[]>('/api/vendedores');
  const r: Record<string, Cliente[]> = {};
  for (const g of grupos) {
    r[g.vendedor] = g.clientes.map(c => ({
      id: c.id,
      nome: c.nome,
      cidade: c.cidade,
      uf: c.uf,
      segmento: c.segmento,
      mix: c.produtosComprados,
    }));
  }
  return r;
}

async function carregarRanking(clienteId: number, p: Parametros): Promise<RankingApi> {
  const q =
    `pesoHistorico=${p.pesoHistorico}&bonusCalor=${p.bonusCalor}&limiteCalor=${p.limiteCalor}` +
    `&bonusUmidade=${p.bonusUmidade}&bonusEvento=${p.bonusEvento}&janelaEventos=${p.janelaEventos}` +
    `&limite=${ITENS_POR_CLIENTE}`;
  const r = await getJson<RespostaApi>(`/api/clientes/${clienteId}/recomendacoes?${q}`);
  return {
    avisos: r.avisos,
    rk: r.recomendacoes.map(i => ({
      produto: { id: i.produtoId, nome: i.produto, familia: i.familia, quente: false, saudavel: false, preco: i.preco },
      pontos: i.pontos,
      partes: i.partes,
      motivos: i.motivos,
    })),
  };
}

export type EstadoApi = {
  /** null = ainda testando; true = usando a API; false = API fora do ar, usando o motor local. */
  online: boolean | null;
  fonteClima?: string;
  vendedores: Record<string, Cliente[]> | null;
  rankings: Record<number, RankingApi>;
  /** Nome de cada produto por id (para mostrar o que o cliente já compra). */
  produtos: Record<number, string>;
};

/**
 * Liga a tela à API: carrega os clientes ao abrir e busca o ranking dos clientes do
 * representante informado sempre que os parâmetros mudam (com uma pequena espera entre toques).
 */
export function useApiFruki(vendedor: string, params: Parametros): EstadoApi {
  const [online, setOnline] = useState<boolean | null>(null);
  const [fonteClima, setFonteClima] = useState<string>();
  const [vendedores, setVendedores] = useState<Record<string, Cliente[]> | null>(null);
  const [rankings, setRankings] = useState<Record<number, RankingApi>>({});
  const [produtos, setProdutos] = useState<Record<number, string>>({});
  const rodada = useRef(0);

  useEffect(() => {
    getJson<{ fonteClima: string }>('/api/status', 3000)
      .then(s => {
        setFonteClima(s.fonteClima);
        return carregarVendedores();
      })
      .then(v => {
        setVendedores(v);
        setOnline(true);
        // Sem os nomes, a tela só mostra a quantidade de produtos do mix.
        getJson<{ id: number; nome: string }[]>('/api/produtos')
          .then(ps => setProdutos(Object.fromEntries(ps.map(p => [p.id, p.nome]))))
          .catch(() => undefined);
      })
      .catch(() => setOnline(false));
  }, []);

  const clientes = vendedores?.[vendedor] ?? [];
  const ids = clientes.map(c => c.id).filter((id): id is number => id != null);
  const chaveIds = ids.join(',');
  const chaveParams = JSON.stringify(params);

  useEffect(() => {
    if (!online || ids.length === 0) return;
    const minha = ++rodada.current;
    const timer = setTimeout(() => {
      Promise.all(ids.map(id => carregarRanking(id, params).then(r => [id, r] as const)))
        .then(lista => {
          if (minha !== rodada.current) return; // chegou uma resposta mais nova
          setRankings(atual => ({ ...atual, ...Object.fromEntries(lista) }));
        })
        .catch(() => {
          if (minha === rodada.current) setOnline(false);
        });
    }, 250);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [online, chaveIds, chaveParams]);

  return { online, fonteClima, vendedores, rankings, produtos };
}
