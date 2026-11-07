// Estado compartilhado pelas abas: vendedor, clientes, rankings, escolhas e visitas.
import { useEffect, useMemo, useState } from 'react';

import { useApiFruki } from '../api/fruki';
import { Cliente, PRODUTOS, VENDEDORES, preco } from '../data/dados';
import { PARAMETROS_PADRAO, Parametros, Recomendacao, ranking } from '../motor/motor';

/** Quantas sugestões o vendedor vê por cliente. */
export const SUGESTOES = 3;

export type Aba = 'inicio' | 'clientes' | 'mapa' | 'vendas' | 'ajustes';

export type CardCliente = {
  c: Cliente;
  /** Identifica o cliente na carteira do vendedor (escolhas, visitas, seleção). */
  chave: string;
  rk: Recomendacao[];
  ids: number[];
  rows: Recomendacao[];
  avisos: string[];
  carregando: boolean;
  /** Soma de 1 caixa de cada sugestão (R$). */
  potencial: number;
};

export type ClienteDaCarteira = Cliente & { vendedor: string };

export const chaveCliente = (vendedor: string, c: Cliente) => `${vendedor}#${c.id ?? c.nome}`;

export const normalizar = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

export function useMotor() {
  const [vendedor, setVendedor] = useState(Object.keys(VENDEDORES)[0]);
  const [params, setParams] = useState<Parametros>(PARAMETROS_PADRAO);
  const [escolhas, setEscolhas] = useState<Record<string, number[]>>({});
  const [visitados, setVisitados] = useState<Record<string, boolean>>({});

  // Com a API no ar, clientes e rankings vêm do Java (motor V2); sem ela, do motor local.
  const api = useApiFruki(vendedor, params);
  const usandoApi = api.online === true && api.vendedores != null;
  const fonte = usandoApi ? api.vendedores! : VENDEDORES;
  const vendedores = Object.keys(fonte);
  const lista = useMemo(() => fonte[vendedor] ?? [], [fonte, vendedor]);

  useEffect(() => {
    if (!fonte[vendedor] && vendedores.length) setVendedor(vendedores[0]);
  }, [fonte, vendedor, vendedores]);

  const cards: CardCliente[] = useMemo(
    () =>
      lista.map(c => {
        const chave = chaveCliente(vendedor, c);
        const daApi = c.id != null ? api.rankings[c.id] : undefined;
        const rk = usandoApi ? daApi?.rk ?? [] : ranking(c, params);
        const ids = (escolhas[chave] ?? rk.slice(0, SUGESTOES).map(r => r.produto.id)).filter(id =>
          rk.some(r => r.produto.id === id),
        );
        const rows = ids.map(id => rk.find(r => r.produto.id === id)!);
        return {
          c,
          chave,
          rk,
          ids,
          rows,
          avisos: daApi?.avisos ?? [],
          carregando: usandoApi && !daApi,
          potencial: rows.reduce((a, r) => a + preco(r.produto), 0),
        };
      }),
    [lista, params, escolhas, vendedor, usandoApi, api.rankings],
  );

  /** Todos os clientes de todos os vendedores (usado no mapa). */
  const todos: ClienteDaCarteira[] = useMemo(
    () => Object.entries(fonte).flatMap(([v, cs]) => cs.map(c => ({ ...c, vendedor: v }))),
    [fonte],
  );

  const total = cards.reduce((a, x) => a + x.potencial, 0);
  const meta = params.metaPorCliente * Math.max(1, lista.length);
  const visitas = cards.filter(x => visitados[x.chave]).length;

  const nomeProduto = (id: number) =>
    api.produtos[id] ?? PRODUTOS.find(p => p.id === id)?.nome ?? `Produto ${id}`;

  const trocarSugestao = (chave: string, slot: number, produtoId: number) => {
    const card = cards.find(x => x.chave === chave);
    if (!card) return;
    const ids = [...card.ids];
    ids[slot] = produtoId;
    setEscolhas({ ...escolhas, [chave]: ids });
  };

  const alternarVisita = (chave: string) => setVisitados({ ...visitados, [chave]: !visitados[chave] });

  return {
    api,
    usandoApi,
    vendedor,
    setVendedor,
    vendedores,
    fonte,
    lista,
    cards,
    todos,
    params,
    setParams,
    total,
    meta,
    pct: Math.min(100, Math.round((total / meta) * 100)),
    skusNovos: new Set(cards.flatMap(x => x.ids)).size,
    visitados,
    visitas,
    alternarVisita,
    nomeProduto,
    trocarSugestao,
  };
}

export type Motor = ReturnType<typeof useMotor>;

/** Ações de navegação que as abas podem pedir ao app. */
export type Nav = {
  irPara: (aba: Aba) => void;
  /** Abre o cliente na aba Clientes (troca de vendedor se for preciso). */
  abrirCliente: (vendedor: string, c: Cliente) => void;
  pedirTroca: (chave: string, slot: number) => void;
  enviarWhats: (card: CardCliente) => void;
  largo: boolean;
};
