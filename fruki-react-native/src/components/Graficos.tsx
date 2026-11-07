// Gráficos desenhados com react-native-svg.
import React, { useState } from 'react';
import { LayoutChangeEvent, Pressable, StyleSheet, Text, View } from 'react-native';
import Svg, { Circle, G, Path, Rect, Text as SvgText } from 'react-native-svg';

import { Dia } from '../motor/clima';
import { Criterio, Recomendacao } from '../motor/motor';
import { cores } from '../theme';

/** Largura disponível do componente (mede no primeiro layout). */
function useLargura(inicial = 300): [number, (e: LayoutChangeEvent) => void] {
  const [w, setW] = useState(inicial);
  return [w, e => setW(e.nativeEvent.layout.width)];
}

// ---------- Clima 14 dias ----------

const SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

export function ClimaGrafico({ dias }: { dias: Dia[] }) {
  const [w, onLayout] = useLargura();
  const H = 210;
  const topo = 22;
  const base = H - 34;
  const alt = base - topo;
  const slot = w / dias.length;
  const tMin = Math.min(...dias.map(d => d.temperatura)) - 2;
  const tMax = Math.max(...dias.map(d => d.temperatura)) + 2;
  const yT = (t: number) => topo + alt * 0.05 + alt * 0.55 * (1 - (t - tMin) / (tMax - tMin));
  const yU = (u: number) => topo + alt * (1 - u / 100);
  const xs = dias.map((_, i) => i * slot + slot / 2);
  const linhaT = dias.map((d, i) => `${i ? 'L' : 'M'}${xs[i]},${yT(d.temperatura)}`).join('');
  const linhaU = dias.map((d, i) => `${i ? 'L' : 'M'}${xs[i]},${yU(d.umidade)}`).join('');
  const hoje = new Date();
  return (
    <View onLayout={onLayout} accessibilityLabel="Previsão de 14 dias com chuva, temperatura e umidade">
      <Svg width={w} height={H}>
        {[0, 1, 2, 3, 4].map(g => (
          <Rect key={g} x={0} y={topo + (alt * g) / 4} width={w} height={1} fill={cores.borda} />
        ))}
        {dias.map((d, i) => (
          <Rect
            key={i}
            x={i * slot + slot * 0.22}
            y={base - (alt * d.chuva) / 100}
            width={slot * 0.56}
            height={(alt * d.chuva) / 100}
            rx={3}
            fill={cores.azul}
            opacity={0.28}
          />
        ))}
        <Path d={linhaU} stroke={cores.verde} strokeWidth={2} strokeDasharray="5 4" fill="none" />
        <Path d={linhaT} stroke={cores.laranja} strokeWidth={2.5} fill="none" strokeLinejoin="round" />
        {dias.map((d, i) => {
          const dia = new Date(hoje.getTime() + (i + 1) * 864e5);
          return (
            <G key={i}>
              <Circle cx={xs[i]} cy={yT(d.temperatura)} r={3} fill={cores.laranja} />
              <SvgText x={xs[i]} y={yT(d.temperatura) - 7} fontSize={10} fill={cores.texto} textAnchor="middle">
                {`${d.temperatura}°`}
              </SvgText>
              <SvgText x={xs[i]} y={base + 14} fontSize={9.5} fill={cores.mut} textAnchor="middle">
                {SEMANA[dia.getDay()]}
              </SvgText>
              <SvgText x={xs[i]} y={base + 27} fontSize={9.5} fill={cores.mut} textAnchor="middle">
                {String(dia.getDate())}
              </SvgText>
            </G>
          );
        })}
      </Svg>
    </View>
  );
}

// ---------- Colunas (vendas) ----------

type BarrasProps = { itens: [string, number][]; onBarra?: (nome: string) => void };

export function Barras({ itens, onBarra }: BarrasProps) {
  const [w, onLayout] = useLargura();
  const H = 240;
  const topo = 20;
  const base = H - 40;
  const max = Math.max(...itens.map(i => i[1]));
  const tot = itens.reduce((a, i) => a + i[1], 0);
  const slot = w / itens.length;
  const last = itens.length - 1;
  return (
    <View onLayout={onLayout} style={{ height: H }}>
      <Svg width={w} height={H}>
        {itens.map(([nome, v], i) => {
          const h = ((base - topo) * v) / max;
          const bw = Math.min(slot * 0.62, 56);
          const x = i * slot + (slot - bw) / 2;
          return (
            <G key={nome}>
              <Rect
                x={x}
                y={base - h}
                width={bw}
                height={h}
                rx={6}
                fill={i === last ? cores.laranja : cores.verde}
                opacity={i === 0 || i === last ? 1 : 0.6}
              />
              <SvgText x={i * slot + slot / 2} y={base - h - 6} fontSize={12} fontWeight="bold" fill={cores.texto} textAnchor="middle">
                {`${Math.round((v / tot) * 100)}%`}
              </SvgText>
            </G>
          );
        })}
      </Svg>
      <View style={[StyleSheet.absoluteFill, { flexDirection: 'row' }]}>
        {itens.map(([nome, v]) => (
          <Pressable
            key={nome}
            disabled={!onBarra}
            onPress={() => onBarra?.(nome)}
            accessibilityRole={onBarra ? 'button' : undefined}
            accessibilityLabel={`${nome}, ${v} mil unidades`}
            style={{ flex: 1, justifyContent: 'flex-end', alignItems: 'center', paddingHorizontal: 2 }}>
            <Text style={s.rotuloBarra} numberOfLines={2}>
              {nome}
            </Text>
          </Pressable>
        ))}
      </View>
    </View>
  );
}

// ---------- Composição da pontuação ----------

export const CORES_CRITERIO: Record<Criterio, string> = {
  'Histórico e segmento': cores.verdeEscuro,
  Cesta: '#7A5AF8',
  Calor: cores.laranja,
  Umidade: cores.azul,
  Eventos: cores.amarelo,
  Ajuste: cores.vermelho,
};

export function Pontuacao({ itens }: { itens: Recomendacao[] }) {
  const maxPos = Math.max(
    1,
    ...itens.map(r => Object.values(r.partes).reduce((a, v) => a + Math.max(0, v ?? 0), 0)),
  );
  return (
    <View>
      <View style={s.legenda}>
        {(Object.keys(CORES_CRITERIO) as Criterio[]).map(k => (
          <View key={k} style={s.legItem}>
            <View style={[s.legCor, { backgroundColor: CORES_CRITERIO[k] }]} />
            <Text style={s.legTxt}>{k}</Text>
          </View>
        ))}
      </View>
      {itens.map(r => {
        const aj = r.partes.Ajuste ?? 0;
        return (
          <View key={r.produto.id} style={{ marginBottom: 10 }} accessibilityLabel={`${r.produto.nome}: ${r.pontos} pontos`}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
              <Text style={s.pNome}>{r.produto.nome}</Text>
              <Text style={[s.pNome, { fontWeight: '700' }]}>{r.pontos} pts</Text>
            </View>
            <View style={s.pTrilho}>
              {(Object.entries(r.partes) as [Criterio, number][])
                .filter(([, v]) => v > 0)
                .map(([k, v]) => (
                  <View key={k} style={{ width: `${(v / maxPos) * 100}%`, backgroundColor: CORES_CRITERIO[k] }} />
                ))}
              {aj < 0 && (
                <View style={{ width: `${(-aj / maxPos) * 100}%`, backgroundColor: cores.vermelho, opacity: 0.7, marginLeft: `${(aj / maxPos) * 100}%` }} />
              )}
            </View>
          </View>
        );
      })}
    </View>
  );
}

const s = StyleSheet.create({
  rotuloBarra: { fontSize: 10.5, color: cores.texto, textAlign: 'center', height: 30 },
  legenda: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: 8 },
  legItem: { flexDirection: 'row', alignItems: 'center', marginRight: 12, marginBottom: 4 },
  legCor: { width: 10, height: 10, borderRadius: 3, marginRight: 5 },
  legTxt: { fontSize: 11, color: cores.mut },
  pNome: { fontSize: 12, color: cores.texto },
  pTrilho: { flexDirection: 'row', height: 12, borderRadius: 6, backgroundColor: cores.trilho, overflow: 'hidden', marginTop: 4 },
});
