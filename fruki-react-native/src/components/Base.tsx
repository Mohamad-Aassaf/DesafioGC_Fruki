// Peças visuais reaproveitadas pelas telas.
import React from 'react';
import { StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native';
import Svg, { Path } from 'react-native-svg';

import { cores } from '../theme';

export function Card({ children, style, destaque }: { children: React.ReactNode; style?: StyleProp<ViewStyle>; destaque?: boolean }) {
  return <View style={[b.card, destaque && b.destaque, style]}>{children}</View>;
}

export function Titulo({ children }: { children: React.ReactNode }) {
  return (
    <Text style={b.titulo} accessibilityRole="header">
      {children}
    </Text>
  );
}

export function Sub({ children, style }: { children: React.ReactNode; style?: StyleProp<any> }) {
  return <Text style={[b.sub, style]}>{children}</Text>;
}

/** Logo estilizado "F" nas cores Fruki. */
export function Logo({ tamanho = 28 }: { tamanho?: number }) {
  return (
    <Svg width={tamanho} height={tamanho} viewBox="0 0 24 24">
      <Path d="M5,3h5v18h-5z" fill={cores.verde} />
      <Path d="M11.5,3h8.5v5h-8.5z" fill={cores.verde} />
      <Path d="M11.5,10h6v4.5h-6z" fill={cores.verdeEscuro} />
    </Svg>
  );
}

export function Icone({ d, cor = '#fff', tamanho = 24 }: { d: string; cor?: string; tamanho?: number }) {
  return (
    <Svg width={tamanho} height={tamanho} viewBox="0 0 24 24">
      <Path d={d} fill={cor} />
    </Svg>
  );
}

export const ICONES = {
  chat: 'M20,2H4C2.9,2 2,2.9 2,4v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z',
};

const b = StyleSheet.create({
  card: {
    backgroundColor: cores.card,
    borderColor: cores.borda,
    borderWidth: 1,
    borderRadius: 20,
    padding: 16,
    marginTop: 14,
  },
  destaque: { borderColor: cores.verde, borderWidth: 2 },
  titulo: { fontSize: 18, fontWeight: '800', color: cores.verdeEscuro },
  sub: { fontSize: 13, color: cores.mut, marginTop: 4, marginBottom: 10 },
});
