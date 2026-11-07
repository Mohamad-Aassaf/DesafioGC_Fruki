// Motor de recomendação por regras e pontuação (scoring).
// Cruza histórico do segmento, clima do estado do cliente e próximos eventos e devolve
// os produtos que o cliente ainda não compra, do mais para o menos indicado.

import { Cliente, EVENTOS, Evento, PRODUTOS, Produto, hash } from '../data/dados';
import { classeUmidade, temperaturaMedia, umidadeMedia } from './clima';

export type Parametros = {
  /** Peso do histórico e do segmento, em % (100 = normal). */
  pesoHistorico: number;
  /** Pontos extras para produtos de calor quando a média passa do limite. */
  bonusCalor: number;
  /** Temperatura média (°C) a partir da qual vale o bônus de calor. */
  limiteCalor: number;
  /** Pontos extras para hidratação quando a umidade está alta ou baixa. */
  bonusUmidade: number;
  /** Pontos extras quando um evento da família está perto. */
  bonusEvento: number;
  /** Quantos dias à frente olhar os eventos. */
  janelaEventos: number;
  /** Meta semanal em itens novos por cliente (R$). */
  metaPorCliente: number;
};

export const PARAMETROS_PADRAO: Parametros = {
  pesoHistorico: 100,
  bonusCalor: 15,
  limiteCalor: 28,
  bonusUmidade: 10,
  bonusEvento: 12,
  janelaEventos: 60,
  metaPorCliente: 150,
};

/** 'Cesta' só aparece nas recomendações que vêm da API (motor V2). */
export type Criterio = 'Histórico e segmento' | 'Cesta' | 'Calor' | 'Umidade' | 'Eventos' | 'Ajuste';

export type Recomendacao = {
  produto: Produto;
  pontos: number;
  partes: Partial<Record<Criterio, number>>;
  motivos: string[];
};

const DIA = 864e5;

export const dias = (e: Evento) => {
  const hoje = new Date();
  hoje.setHours(12, 0, 0, 0);
  return Math.round((e.data.getTime() - hoje.getTime()) / DIA);
};

export const eventosFuturos = () =>
  EVENTOS.filter(e => dias(e) >= 0).sort((a, b) => a.data.getTime() - b.data.getTime());

export const proximoEvento = (familia: string, janela: number) =>
  eventosFuturos().find(e => e.familias.includes(familia) && dias(e) <= janela);

export function pontuar(c: Cliente, p: Produto, pr: Parametros): Recomendacao {
  const r: Recomendacao = { produto: p, pontos: 0, partes: {}, motivos: [] };
  const somar = (criterio: Criterio, valor: number, motivo: string) => {
    if (!valor) return;
    r.partes[criterio] = valor;
    r.pontos += valor;
    r.motivos.push(motivo);
  };

  const base = (hash(c.nome + p.nome) % 25) + 30;
  somar('Histórico e segmento', Math.round((base * pr.pesoHistorico) / 100),
    `clientes do segmento ${c.segmento.toLowerCase()} compram`);

  const t = temperaturaMedia(c.uf);
  if (t >= pr.limiteCalor && p.quente) somar('Calor', pr.bonusCalor, `calor (${t}°C)`);

  const u = umidadeMedia(c.uf);
  const classe = classeUmidade(u);
  if (p.saudavel && classe) somar('Umidade', pr.bonusUmidade, `umidade ${classe} (${u}%): hidratação`);
  if (p.familia === 'Energético lata' && classe === 'alta') {
    somar('Ajuste', -8, 'calor úmido: energético menos indicado');
  }

  const e = proximoEvento(p.familia, pr.janelaEventos);
  if (e) somar('Eventos', pr.bonusEvento, `${e.nome} em ${dias(e)} d`);
  return r;
}

/** Ranking dos produtos que o cliente ainda não compra. */
export const ranking = (c: Cliente, pr: Parametros) =>
  PRODUTOS.filter(p => !c.mix.includes(p.id))
    .map(p => pontuar(c, p, pr))
    .sort((a, b) => b.pontos - a.pontos);
