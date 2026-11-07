// Dados simulados do protótipo: estados, produtos, vendas, eventos e clientes.

export type Produto = {
  id: number;
  nome: string;
  familia: string;
  /** Vende mais no calor. */
  quente: boolean;
  /** Opção de hidratação / sem açúcar. */
  saudavel: boolean;
  /** Preço da caixa vindo da API; sem ele, usa o preço simulado. */
  preco?: number;
};

export type Familia = { nome: string; volume: number; produtos: Record<string, number> };

export type Evento = { data: Date; nome: string; abrangencia: string; familias: string[] };

export type Cliente = {
  /** Id na API (só existe quando os dados vêm do servidor). */
  id?: number;
  nome: string;
  cidade: string;
  uf: string;
  segmento: string;
  /** Ids dos produtos que o cliente já compra. */
  mix: number[];
};

/** Contorno simplificado dos estados: pares "longitude,latitude". */
export const CONTORNOS: Record<string, string> = {
  RR: '-64.8,1.6 -64,3.8 -63.4,4.2 -61.5,5 -60.7,5.2 -60.2,5.25 -59.8,4.4 -60,3 -59.2,2.4 -58.5,1.3 -58.9,0.4 -60.5,-1.1 -62,0.3 -63.5,1',
  AP: '-54,2.3 -52.4,2.9 -51.6,4.4 -51,3 -50.4,1.7 -50,0.8 -50.3,0.1 -51,-0.5 -52.6,-1.2 -53.2,0.3',
  PA: '-54,2.3 -53.2,0.3 -52.6,-1.2 -51,-0.5 -50.3,0.1 -49.5,-0.2 -48.5,-0.8 -47.3,-0.6 -46.1,-1.1 -46.2,-3.2 -47.2,-4.2 -48.4,-5.4 -48.8,-7 -49.5,-8.5 -50.2,-9.3 -53.5,-8.9 -55.8,-7.8 -58.4,-7.3 -58.2,-4.5 -57.2,-2.3 -57.8,0 -58.5,1.3 -56.5,2',
  AM: '-73.8,-7.3 -72,-7.5 -70,-7.6 -68.5,-8.5 -66.7,-9.8 -63.5,-8.5 -60.4,-8.8 -58.4,-7.3 -58.2,-4.5 -57.2,-2.3 -57.8,0 -58.9,0.4 -60.5,-1.1 -62,0.3 -63.5,1 -64.8,1.6 -67,2 -69,1.1 -70,0.5 -69.5,-1.2 -70,-4.2',
  AC: '-73.8,-7.3 -72,-7.5 -70,-7.6 -68.5,-8.5 -66.7,-9.8 -67.5,-10.5 -68.7,-11.1 -70.5,-11 -72.2,-9.9 -73,-8.8',
  RO: '-66.7,-9.8 -63.5,-8.5 -60.4,-8.8 -60.3,-11.5 -60.4,-13.5 -62.5,-13 -64.5,-12.3 -65.4,-11.3 -65,-10',
  MT: '-58.4,-7.3 -55.8,-7.8 -53.5,-8.9 -50.2,-9.3 -50.6,-11.5 -50.5,-13 -52,-15 -53.2,-17.8 -55,-17.2 -57.5,-17.6 -58.2,-16.3 -60,-15.2 -60.4,-13.5 -60.3,-11.5 -60.4,-8.8',
  TO: '-48.4,-5.4 -48.8,-7 -49.5,-8.5 -50.2,-9.3 -50.6,-11.5 -50.5,-13 -48.5,-13.3 -46.5,-12.5 -46,-10.5 -46,-10 -47.4,-8 -47.7,-5.8',
  MA: '-46.1,-1.1 -45,-1.5 -44,-2.4 -42.5,-2.7 -41.8,-2.8 -42.8,-4 -43.5,-6 -44.2,-7 -45.4,-9 -46,-10 -47.4,-8 -47.7,-5.8 -48.4,-5.4 -47.2,-4.2 -46.2,-3.2',
  PI: '-41.8,-2.8 -41.2,-3 -41.4,-4.2 -40.5,-6 -40.8,-7.5 -41,-9 -41.4,-9.5 -43,-10.8 -45.8,-10.7 -45.4,-9 -44.2,-7 -43.5,-6 -42.8,-4',
  CE: '-41.2,-3 -39.5,-2.9 -38.5,-3.7 -37.3,-4.6 -38,-5.6 -38.6,-6.8 -39.2,-7.8 -40.5,-7.3 -40.5,-6 -41.4,-4.2',
  RN: '-37.3,-4.6 -35.5,-5.1 -35.1,-5.8 -35,-6.5 -36.7,-6.5 -38,-6.2 -38,-5.6',
  PB: '-35,-6.5 -34.8,-7.2 -35.2,-7.5 -37,-8.1 -38.6,-7.2 -38.6,-6.8 -38,-6.2 -36.7,-6.5',
  PE: '-34.9,-7.4 -34.9,-8.2 -35.3,-9.2 -36.5,-9.5 -37.8,-9.2 -38.8,-9 -40,-8.8 -41,-9 -40.8,-7.5 -40.5,-7.3 -39.2,-7.8 -38.6,-7.2 -37,-8.1 -35.2,-7.5',
  AL: '-35.3,-9.2 -35.9,-9.9 -36.4,-10.5 -37,-10 -37.8,-9.3 -37.8,-9.2 -36.5,-9.5',
  SE: '-36.4,-10.5 -37,-11 -37.4,-11.6 -38.3,-10.9 -38,-10 -37.8,-9.4 -37,-10',
  BA: '-46,-10 -45.8,-10.7 -43,-10.8 -41.4,-9.5 -40,-8.8 -38.8,-9 -37.8,-9.4 -38,-10 -38.3,-10.9 -37.4,-11.6 -38.5,-13 -39,-14.8 -39,-17 -39.7,-18.3 -39.9,-18.2 -40.5,-17.3 -41.5,-16.5 -42.5,-15.7 -43.8,-15 -44.2,-14.2 -46.4,-14.3 -46.5,-12.5 -46,-10.5',
  GO: '-50.5,-13 -48.5,-13.3 -46.5,-12.5 -46.4,-14.3 -46.2,-16 -47.5,-17.5 -48.9,-18.4 -50,-18.9 -51,-19 -52.5,-18.5 -53.2,-17.8 -52,-15',
  DF: '-48.2,-15.5 -47.3,-15.5 -47.3,-16.05 -48.2,-16.05',
  MG: '-51,-19 -51,-20 -50,-20 -48.2,-20 -47.2,-20.7 -46.8,-22 -45.5,-22.6 -44.6,-22.3 -43.5,-22 -42.5,-21.1 -41.8,-21 -41,-20.5 -40.8,-19.5 -39.9,-18.2 -40.5,-17.3 -41.5,-16.5 -42.5,-15.7 -43.8,-15 -44.2,-14.2 -46.4,-14.3 -46.2,-16 -47.5,-17.5 -48.9,-18.4 -50,-18.9',
  ES: '-39.7,-18.3 -39.9,-18.2 -40.8,-19.5 -41,-20.5 -41.8,-21 -41,-21.3 -40.5,-20.7 -40,-19.5',
  RJ: '-41,-21.3 -41.8,-21 -42.5,-21.1 -43.5,-22 -44.6,-22.3 -44.7,-23.2 -43.2,-23 -42,-22.9 -41,-22',
  SP: '-44.6,-22.3 -45.5,-22.6 -46.8,-22 -47.2,-20.7 -48.2,-20 -51,-20 -52,-21.8 -53,-22.6 -51.5,-22.7 -50,-23.5 -49.2,-24 -48.2,-24.8 -48,-25.3 -46.5,-24.2 -45,-23.5 -44.7,-23.2',
  MS: '-53.2,-17.8 -51,-19 -51,-20 -52,-21.8 -53,-22.6 -54.3,-24 -55,-23.8 -55.7,-22.2 -57.8,-22 -57.6,-19 -57.5,-17.6 -55,-17.2',
  PR: '-53,-22.6 -51.5,-22.7 -50,-23.5 -49.2,-24 -48.2,-24.8 -48,-25.3 -48.6,-26.1 -50,-26.4 -51.5,-26.1 -53.8,-25.9 -54.6,-25.5 -54.3,-24.1',
  SC: '-48.6,-26.1 -48.5,-27.6 -48.8,-28.5 -49.7,-29.3 -50.5,-29 -51.5,-28.4 -52.5,-27.5 -53.8,-27.1 -53.8,-25.9 -51.5,-26.1 -50,-26.4',
  RS: '-49.7,-29.3 -50.2,-30.4 -51.2,-31.4 -52.2,-32.2 -53.4,-33.7 -53.2,-32.8 -54.5,-31.6 -55.5,-30.9 -57.6,-30.2 -56,-28.6 -54.5,-27.3 -53.8,-27.1 -52.5,-27.5 -51.5,-28.4 -50.5,-29',
};

const REGIOES: Record<string, string> = {
  N: 'RR AP AM PA AC RO TO',
  NE: 'MA PI CE RN BA PE PB SE AL',
  CO: 'MT GO DF MS',
  SE: 'MG SP RJ ES',
  S: 'PR SC RS',
};

export type RegiaoId = 'S' | 'SE' | 'CO' | 'NE' | 'N';

/** Regiões na ordem do tour do mapa: começa pelo Sul, onde estão os principais clientes. */
export const REGIOES_TOUR: { id: RegiaoId; nome: string; ufs: string[] }[] = [
  { id: 'S', nome: 'Sul', ufs: ['RS', 'SC', 'PR'] },
  { id: 'SE', nome: 'Sudeste', ufs: ['SP', 'RJ', 'MG', 'ES'] },
  { id: 'CO', nome: 'Centro-Oeste', ufs: ['MS', 'MT', 'GO', 'DF'] },
  { id: 'NE', nome: 'Nordeste', ufs: ['BA', 'SE', 'AL', 'PE', 'PB', 'RN', 'CE', 'PI', 'MA'] },
  { id: 'N', nome: 'Norte', ufs: ['TO', 'PA', 'AP', 'AM', 'RR', 'RO', 'AC'] },
];

export const NOME_UF: Record<string, string> = {
  AC: 'Acre', AL: 'Alagoas', AP: 'Amapá', AM: 'Amazonas', BA: 'Bahia', CE: 'Ceará', DF: 'Distrito Federal',
  ES: 'Espírito Santo', GO: 'Goiás', MA: 'Maranhão', MT: 'Mato Grosso', MS: 'Mato Grosso do Sul',
  MG: 'Minas Gerais', PA: 'Pará', PB: 'Paraíba', PR: 'Paraná', PE: 'Pernambuco', PI: 'Piauí',
  RJ: 'Rio de Janeiro', RN: 'Rio Grande do Norte', RS: 'Rio Grande do Sul', RO: 'Rondônia', RR: 'Roraima',
  SC: 'Santa Catarina', SP: 'São Paulo', SE: 'Sergipe', TO: 'Tocantins',
};

/** Região -> [temperatura base, umidade base]. */
export const BASE: Record<string, [number, number]> = {
  N: [32, 85],
  NE: [31, 72],
  CO: [30, 48],
  SE: [28, 65],
  S: [23, 75],
};

export const regiao = (uf: string): string =>
  Object.keys(REGIOES).find(r => REGIOES[r].split(' ').includes(uf)) ?? 'S';

/** Soma dos códigos dos caracteres: dá números estáveis para simular dados. */
export const hash = (s: string): number => {
  let a = 0;
  for (let i = 0; i < s.length; i++) a += s.charCodeAt(i);
  return a;
};

export const FAMILIAS: Familia[] = [
  { nome: 'Refrigerante PET', volume: 78, produtos: { 'Guaraná 2L': 32, 'Cola 2L': 21, 'Laranja 2L': 13, 'Guaraná Zero 2L': 12 } },
  { nome: 'Refrigerante lata', volume: 45, produtos: { 'Guaraná 350 ml': 19, 'Cola 350 ml': 14, 'Uva 350 ml': 12 } },
  { nome: 'Energético lata', volume: 30, produtos: { 'Elev 473 ml': 18, 'Elev Zero 473 ml': 12 } },
  { nome: 'Água', volume: 38, produtos: { 'Sem gás 1,5 L': 22, 'Com gás 500 ml': 16 } },
  { nome: 'Chás e sucos', volume: 22, produtos: { 'Chá limão 1,5 L': 13, 'Chá pêssego 1,5 L': 9 } },
];

export const PRODUTOS: Produto[] = [
  { id: 1, nome: 'Guaraná 2L', familia: 'Refrigerante PET', quente: true, saudavel: false },
  { id: 2, nome: 'Cola 2L', familia: 'Refrigerante PET', quente: true, saudavel: false },
  { id: 3, nome: 'Guaraná 350 ml', familia: 'Refrigerante lata', quente: true, saudavel: false },
  { id: 4, nome: 'Elev 473 ml', familia: 'Energético lata', quente: false, saudavel: false },
  { id: 5, nome: 'Água sem gás 1,5 L', familia: 'Água', quente: true, saudavel: true },
  { id: 6, nome: 'Chá limão 1,5 L', familia: 'Chás e sucos', quente: true, saudavel: true },
  { id: 7, nome: 'Guaraná Zero 2L', familia: 'Refrigerante PET', quente: false, saudavel: true },
  { id: 8, nome: 'Uva 350 ml', familia: 'Refrigerante lata', quente: false, saudavel: false },
];

export const preco = (p: Produto): number => p.preco ?? 20 + (hash(p.nome) % 40);

const ev = (d: string, nome: string, abrangencia: string, familias: string[]): Evento => ({
  data: new Date(d + 'T12:00:00'),
  nome,
  abrangencia,
  familias,
});

export const EVENTOS: Evento[] = [
  ev('2026-11-02', 'Finados', 'Nacional', []),
  ev('2026-11-20', 'Consciência Negra', 'Nacional', []),
  ev('2026-11-27', 'Black Friday', 'Nacional', ['Refrigerante PET', 'Refrigerante lata']),
  ev('2026-12-25', 'Natal', 'Nacional', ['Refrigerante PET', 'Refrigerante lata']),
  ev('2026-12-31', 'Ano Novo', 'Nacional', ['Energético lata', 'Refrigerante lata', 'Água']),
  ev('2027-02-02', 'Festa de Iemanjá', 'Sul e Nordeste', ['Água']),
  ev('2027-02-09', 'Carnaval', 'Nacional', ['Água', 'Energético lata', 'Refrigerante lata']),
  ev('2027-03-28', 'Páscoa', 'Nacional', ['Chás e sucos']),
  ev('2027-05-09', 'Dia das Mães', 'Nacional', ['Refrigerante PET', 'Chás e sucos']),
];

const cli = (nome: string, cidade: string, uf: string, segmento: string, mix: number[]): Cliente => ({
  nome,
  cidade,
  uf,
  segmento,
  mix,
});

export const VENDEDORES: Record<string, Cliente[]> = {
  'Rep. 33307 (RS)': [
    cli('Mercado Pinheiro', 'Canoas', 'RS', 'Mercado', [1, 2, 5]),
    cli('Padaria Trigo Bom', 'Porto Alegre', 'RS', 'Padaria', [1, 3]),
    cli('Supermercado Lajeado', 'Lajeado', 'RS', 'Supermercado', [1, 2, 3, 4]),
    cli('Minimercado Sol', 'Gravataí', 'RS', 'Mercadinho', [2]),
  ],
  'Rep. 31402 (Nordeste)': [
    cli('Mercadinho Central', 'Salvador', 'BA', 'Mercadinho', [1, 3]),
    cli('Atacado Recife Sul', 'Recife', 'PE', 'Atacado', [1, 2, 4]),
    cli('Bar da Praça', 'Fortaleza', 'CE', 'Bar', [3]),
  ],
  'Rep. 34120 (SC/PR)': [
    cli('Mercado Beira-Mar', 'Florianópolis', 'SC', 'Mercado', [1, 2, 3, 5]),
    cli('Supermercado Araucária', 'Curitiba', 'PR', 'Supermercado', [1, 2, 3, 5, 6]),
  ],
  'Rep. 35200 (Sudeste e Centro-Oeste)': [
    cli('Mercado Paulista', 'Campinas', 'SP', 'Mercado', [1, 2, 5]),
    cli('Atacado Cerrado', 'Goiânia', 'GO', 'Atacado', [1, 2, 3, 4, 5]),
  ],
  'Rep. 30110 (Norte)': [
    cli('Mercado Rio Negro', 'Manaus', 'AM', 'Mercado', [1, 2]),
    cli('Distribuidora Belém', 'Belém', 'PA', 'Atacado', [1, 3, 5]),
  ],
};
