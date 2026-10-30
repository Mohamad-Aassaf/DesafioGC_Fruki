import { BASE, hash, regiao } from '../data/dados';

export type Dia = { temperatura: number; chuva: number; umidade: number };

const cache: Record<string, Dia[]> = {};

/** Previsão simulada de 14 dias para a UF. */
export function previsao(uf: string): Dia[] {
  if (cache[uf]) return cache[uf];
  const h = hash(uf);
  const [t0, u0] = BASE[regiao(uf)];
  const dias = Array.from({ length: 14 }, (_, d) => {
    const chuva = ((h * 7 + d * 13) % 55) + (t0 < 25 ? 15 : 0);
    return {
      temperatura: Math.round(t0 + 3 * Math.sin((d + h) * 0.9) + (h % 3) - 1),
      chuva,
      umidade: Math.min(98, Math.round(u0 + chuva / 5 + 6 * Math.sin((d + h) * 0.7))),
    };
  });
  cache[uf] = dias;
  return dias;
}

export const temperaturaMedia = (uf: string) =>
  Math.round(previsao(uf).reduce((a, x) => a + x.temperatura, 0) / 14);

export const umidadeMedia = (uf: string) =>
  Math.round(previsao(uf).reduce((a, x) => a + x.umidade, 0) / 14);

export type ClasseUmidade = 'alta' | 'baixa' | null;

export const classeUmidade = (u: number): ClasseUmidade => (u >= 75 ? 'alta' : u <= 45 ? 'baixa' : null);

export const dicaUmidade = (c: ClasseUmidade) =>
  c === 'alta'
    ? 'o suor evapora pouco, priorize água e bebidas sem açúcar.'
    : c === 'baixa'
      ? 'ar seco desidrata, priorize água e chás.'
      : '';

/** Escala de cor do mapa: azul (frio) até laranja (quente). */
export const corTemperatura = (t: number) =>
  `hsl(${Math.max(8, Math.min(215, 215 - (t - 18) * 14))}, 62%, 44%)`;
