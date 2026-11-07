// Mapa do Brasil com zoom animado por região (Sul, Sudeste, Centro-Oeste, Nordeste, Norte).
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { LayoutChangeEvent, View } from 'react-native';
import Svg, { Circle, G, Polygon, Text as SvgText } from 'react-native-svg';

import { CONTORNOS, REGIOES_TOUR, RegiaoId } from '../data/dados';
import { corTemperatura, temperaturaMedia } from '../motor/clima';
import { cores } from '../theme';

type Caixa = [number, number, number, number]; // x, y, largura, altura

const projetar = (o: number, a: number): [number, number] => [(o + 74) * 9.7, (5.3 - a) * 10];

const ESTADOS = Object.entries(CONTORNOS).map(([uf, s]) => {
  const pts = s.split(' ').map(p => {
    const [o, a] = p.split(',').map(Number);
    return projetar(o, a);
  });
  const cx = pts.reduce((a, p) => a + p[0], 0) / pts.length;
  const cy = pts.reduce((a, p) => a + p[1], 0) / pts.length;
  const regiao = REGIOES_TOUR.find(r => r.ufs.includes(uf))!.id;
  return { uf, pts, pontos: pts.map(p => p.join(',')).join(' '), cx, cy, regiao };
});

/** Retângulo que envolve os estados, com uma folga em volta. */
function caixaDe(ufs: string[] | null): Caixa {
  const pts = ESTADOS.filter(e => !ufs || ufs.includes(e.uf)).flatMap(e => e.pts);
  const xs = pts.map(p => p[0]);
  const ys = pts.map(p => p[1]);
  const [x0, x1, y0, y1] = [Math.min(...xs), Math.max(...xs), Math.min(...ys), Math.max(...ys)];
  const folga = Math.max(x1 - x0, y1 - y0) * (ufs ? 0.1 : 0.03);
  return [x0 - folga, y0 - folga, x1 - x0 + 2 * folga, y1 - y0 + 2 * folga];
}

/** Aumenta a caixa para ter a mesma proporção da área do mapa (evita faixas vazias). */
function ajustarProporcao([x, y, w, h]: Caixa, proporcao: number): Caixa {
  if (w / h < proporcao) {
    const nw = h * proporcao;
    return [x - (nw - w) / 2, y, nw, h];
  }
  const nh = w / proporcao;
  return [x, y - (nh - h) / 2, w, nh];
}

const suave = (t: number) => (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);

type Props = {
  /** Região em foco; null = Brasil inteiro. */
  regiao: RegiaoId | null;
  uf: string;
  onEstado: (uf: string) => void;
  /** Quantos clientes há em cada UF (aparece como um selo no estado). */
  clientesPorUf: Record<string, number>;
  altura?: number;
  duracaoMs?: number;
};

export function MapaRegioes({ regiao, uf, onEstado, clientesPorUf, altura = 420, duracaoMs = 1100 }: Props) {
  const [largura, setLargura] = useState(0);
  const proporcao = largura > 0 ? largura / altura : 1;
  const alvo = useMemo(() => {
    const ufs = regiao ? REGIOES_TOUR.find(r => r.id === regiao)!.ufs : null;
    return ajustarProporcao(caixaDe(ufs), proporcao);
  }, [regiao, proporcao]);

  // Começa no Brasil inteiro e anima até a região pedida.
  const [vb, setVb] = useState<Caixa>(() => ajustarProporcao(caixaDe(null), 1));
  const atual = useRef(vb);
  useEffect(() => {
    const de = atual.current;
    const inicio = Date.now();
    let quadro = 0;
    const passo = () => {
      const t = Math.min(1, (Date.now() - inicio) / duracaoMs);
      const k = suave(t);
      const novo = de.map((v, i) => v + (alvo[i] - v) * k) as Caixa;
      atual.current = novo;
      setVb(novo);
      if (t < 1) quadro = requestAnimationFrame(passo);
    };
    quadro = requestAnimationFrame(passo);
    return () => cancelAnimationFrame(quadro);
  }, [alvo, duracaoMs]);

  const onLayout = (e: LayoutChangeEvent) => setLargura(e.nativeEvent.layout.width);
  // Quantas unidades do mapa cabem em 1 pixel: mantém textos e bordas com tamanho fixo na tela.
  const px = largura > 0 ? vb[2] / largura : 1;
  const zoom = caixaDe(null)[2] / vb[2];
  const fonte = Math.max(9, Math.min(14, 9 * Math.sqrt(zoom)));
  // Estado escolhido por cima (borda inteira visível); o DF, que fica dentro de GO, sempre por último.
  const camada = (x: string) => (x === 'DF' ? 2 : x === uf ? 1 : 0);
  const ordem = [...ESTADOS].sort((a, b) => camada(a.uf) - camada(b.uf));

  return (
    <View onLayout={onLayout} style={{ height: altura }} accessibilityLabel="Mapa do Brasil por região e estado">
      {largura > 0 && (
        <Svg width={largura} height={altura} viewBox={vb.join(' ')}>
          {ordem.map(e => {
            const foco = !regiao || e.regiao === regiao;
            const sel = e.uf === uf;
            const qtd = clientesPorUf[e.uf] ?? 0;
            const mostrarRotulo = foco || zoom < 1.4;
            return (
              <G key={e.uf} onPress={() => onEstado(e.uf)}>
                <Polygon
                  points={e.pontos}
                  fill={foco ? corTemperatura(temperaturaMedia(e.uf)) : '#D5DDD8'}
                  opacity={foco ? 1 : 0.55}
                  stroke={sel ? cores.verdeEscuro : '#FFFFFF'}
                  strokeWidth={(sel ? 3 : 1) * px}
                  strokeLinejoin="round"
                />
                {mostrarRotulo && (
                  <SvgText
                    x={e.cx}
                    y={e.cy + fonte * 0.35 * px}
                    fontSize={fonte * px}
                    fontWeight="bold"
                    fill="#FFFFFF"
                    textAnchor="middle">
                    {e.uf}
                  </SvgText>
                )}
                {foco && zoom > 1.25 && (
                  <SvgText
                    x={e.cx}
                    y={e.cy + fonte * 1.5 * px}
                    fontSize={fonte * 0.8 * px}
                    fill="#FFFFFF"
                    textAnchor="middle">
                    {`${temperaturaMedia(e.uf)}°C`}
                  </SvgText>
                )}
                {foco && qtd > 0 && (
                  <G>
                    <Circle
                      cx={e.cx + fonte * 1.5 * px}
                      cy={e.cy - fonte * 1.2 * px}
                      r={fonte * 0.85 * px}
                      fill={cores.verdeEscuro}
                      stroke="#FFFFFF"
                      strokeWidth={1.5 * px}
                    />
                    <SvgText
                      x={e.cx + fonte * 1.5 * px}
                      y={e.cy - fonte * 0.9 * px}
                      fontSize={fonte * 0.85 * px}
                      fontWeight="bold"
                      fill="#FFFFFF"
                      textAnchor="middle">
                      {String(qtd)}
                    </SvgText>
                  </G>
                )}
              </G>
            );
          })}
        </Svg>
      )}
    </View>
  );
}
