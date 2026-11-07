// Peças de interface usadas pelas abas (chips, indicadores, botões, barras de progresso).
import React from 'react';
import { Pressable, StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native';

import { Icone } from '../components/Base';
import { cores } from '../theme';

/** Estilo de Pressable com realce ao passar o mouse (web) e ao tocar. */
export const realce =
  (base: StyleProp<ViewStyle>, hover: ViewStyle = { backgroundColor: cores.verdeClaro }) =>
  // `hovered` existe no react-native-web, mas não nos tipos do React Native.
  ({ pressed, hovered }: any): StyleProp<ViewStyle> => [base, (hovered || pressed) && hover, pressed && { opacity: 0.85 }];

export function Chip({ ativo, texto, onPress, contador }: { ativo: boolean; texto: string; onPress: () => void; contador?: number }) {
  return (
    <Pressable
      onPress={onPress}
      style={realce([u.chip, ativo && u.chipAtivo], ativo ? {} : { borderColor: cores.verde })}
      accessibilityRole="button"
      accessibilityState={{ selected: ativo }}>
      <Text style={{ color: ativo ? '#fff' : cores.texto, fontWeight: ativo ? '700' : '500', fontSize: 13 }}>
        {texto}
        {contador != null && <Text style={{ opacity: 0.7 }}>{`  ${contador}`}</Text>}
      </Text>
    </Pressable>
  );
}

export function Kpi({
  rotulo,
  valor,
  detalhe,
  icone,
  cor = cores.verde,
  children,
}: {
  rotulo: string;
  valor: string;
  detalhe?: string;
  icone: string;
  cor?: string;
  children?: React.ReactNode;
}) {
  return (
    <View style={u.kpi}>
      <View style={[u.kpiIcone, { backgroundColor: cor + '1A' }]}>
        <Icone d={icone} cor={cor} tamanho={20} />
      </View>
      <View style={{ flex: 1 }}>
        <Text style={u.kpiRotulo}>{rotulo}</Text>
        <Text style={u.kpiValor}>{valor}</Text>
        {detalhe != null && <Text style={u.pequeno}>{detalhe}</Text>}
        {children}
      </View>
    </View>
  );
}

type TipoBotao = 'primario' | 'contorno' | 'tonal';

export function Botao({
  texto,
  onPress,
  tipo = 'primario',
  icone,
  style,
}: {
  texto: string;
  onPress: () => void;
  tipo?: TipoBotao;
  icone?: string;
  style?: StyleProp<ViewStyle>;
}) {
  const corTxt = tipo === 'primario' ? '#fff' : cores.verdeEscuro;
  const hover: ViewStyle =
    tipo === 'primario' ? { backgroundColor: cores.verdeMedio } : { backgroundColor: cores.verdeClaro, borderColor: cores.verde };
  return (
    <Pressable onPress={onPress} accessibilityRole="button" style={realce([u.botao, u[tipo], style], hover)}>
      {icone && <Icone d={icone} cor={corTxt} tamanho={18} />}
      <Text style={{ color: corTxt, fontWeight: '700', fontSize: 14 }}>{texto}</Text>
    </Pressable>
  );
}

export function Progresso({ pct, cor = cores.verde, altura = 8 }: { pct: number; cor?: string; altura?: number }) {
  return (
    <View
      style={[u.trilho, { height: altura }]}
      accessibilityRole="progressbar"
      accessibilityValue={{ min: 0, max: 100, now: Math.round(pct) }}>
      <View style={{ width: `${Math.max(0, Math.min(100, pct))}%`, height: '100%', backgroundColor: cor, borderRadius: 6 }} />
    </View>
  );
}

export function Selo({ texto, cor = cores.verdeMedio, fundo = cores.verdeClaro }: { texto: string; cor?: string; fundo?: string }) {
  return <Text style={[u.selo, { color: cor, backgroundColor: fundo }]}>{texto}</Text>;
}

export function Pontos({ valor }: { valor: number }) {
  return <Text style={u.pontos}>{valor}</Text>;
}

/** Área com largura máxima, centralizada (telas largas). */
export function Conteudo({ children }: { children: React.ReactNode }) {
  return <View style={{ width: '100%', maxWidth: 1240, alignSelf: 'center' }}>{children}</View>;
}

/** Duas colunas em tela larga; uma embaixo da outra no celular. */
export function Colunas({ largo, children, proporcao = [3, 2] }: { largo: boolean; children: React.ReactNode; proporcao?: [number, number] }) {
  const [a, b] = React.Children.toArray(children);
  if (!largo) {
    return (
      <View>
        {a}
        {b}
      </View>
    );
  }
  return (
    <View style={{ flexDirection: 'row', gap: 16, alignItems: 'flex-start' }}>
      <View style={{ flex: proporcao[0], minWidth: 0 }}>{a}</View>
      <View style={{ flex: proporcao[1], minWidth: 0 }}>{b}</View>
    </View>
  );
}

export const u = StyleSheet.create({
  linha: { flexDirection: 'row', alignItems: 'center' },
  pequeno: { fontSize: 12, color: cores.mut },
  forte: { fontSize: 15, fontWeight: '700', color: cores.texto },
  chip: {
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 7,
    backgroundColor: '#fff',
  },
  chipAtivo: { backgroundColor: cores.verde, borderColor: cores.verde },
  kpi: {
    flexGrow: 1,
    flexBasis: 160,
    flexDirection: 'row',
    gap: 10,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: 16,
    padding: 14,
  },
  kpiIcone: { width: 38, height: 38, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  kpiRotulo: { fontSize: 12, color: cores.mut, fontWeight: '600' },
  kpiValor: { fontSize: 22, fontWeight: '800', color: cores.verdeEscuro, marginTop: 2 },
  botao: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderRadius: 12,
    paddingVertical: 11,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: 'transparent',
  },
  primario: { backgroundColor: cores.verde },
  contorno: { backgroundColor: '#fff', borderColor: cores.borda },
  tonal: { backgroundColor: cores.verdeClaro },
  trilho: { borderRadius: 6, backgroundColor: cores.trilho, overflow: 'hidden' },
  selo: {
    fontSize: 11,
    fontWeight: '700',
    borderRadius: 20,
    paddingHorizontal: 8,
    paddingVertical: 2,
    overflow: 'hidden',
    alignSelf: 'flex-start',
  },
  pontos: {
    backgroundColor: cores.verde,
    color: '#fff',
    fontWeight: '800',
    fontSize: 13,
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 3,
    overflow: 'hidden',
    minWidth: 38,
    textAlign: 'center',
  },
});
