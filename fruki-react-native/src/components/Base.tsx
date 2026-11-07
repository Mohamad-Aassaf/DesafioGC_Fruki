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
  inicio: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
  clientes:
    'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  mapa: 'M20.5 3l-.16.03L15 5.1 9 3 3.36 4.9c-.21.07-.36.25-.36.48V20.5c0 .28.22.5.5.5l.16-.03L9 18.9l6 2.1 5.64-1.9c.21-.07.36-.25.36-.48V3.5c0-.28-.22-.5-.5-.5zM15 19l-6-2.11V5l6 2.11V19z',
  vendas: 'M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z',
  ajustes:
    'M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z',
  play: 'M8 5v14l11-7z',
  pausa: 'M6 19h4V5H6v14zm8-14v14h4V5h-4z',
  esquerda: 'M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z',
  direita: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  local:
    'M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z',
  busca:
    'M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z',
  calendario:
    'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  gota: 'M12 2c-5.33 4.55-8 8.48-8 11.8 0 4.98 3.8 8.2 8 8.2s8-3.22 8-8.2c0-3.32-2.67-7.25-8-11.8z',
  sol: 'M12 7a5 5 0 100 10 5 5 0 000-10zM11 1h2v3h-2zm0 19h2v3h-2zM3.51 4.93l1.42-1.42 2.12 2.12-1.42 1.42zm12.02 12.02l1.42-1.42 2.12 2.12-1.42 1.42zM1 11h3v2H1zm19 0h3v2h-3zM4.93 20.49l-1.42-1.42 2.12-2.12 1.42 1.42zM16.95 6.47l2.12-2.12 1.42 1.42-2.12 2.12z',
  troca: 'M16 17.01V10h-2v7.01h-3L15 21l4-3.99h-3zM9 3L5 6.99h3V14h2V6.99h3L9 3z',
  check: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
  mundo:
    'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z',
};

const b = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderColor: cores.borda,
    borderWidth: 1,
    borderRadius: 18,
    padding: 18,
    marginTop: 16,
    shadowColor: '#0B3B2C',
    shadowOpacity: 0.06,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 3 },
  },
  destaque: { borderColor: cores.verde, borderWidth: 2 },
  titulo: { fontSize: 17, fontWeight: '800', color: cores.verdeEscuro },
  sub: { fontSize: 13, color: cores.mut, marginTop: 4, marginBottom: 10 },
});
