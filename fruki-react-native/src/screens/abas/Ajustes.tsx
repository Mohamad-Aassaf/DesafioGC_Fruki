// Ajustes: pesos do motor de recomendação e de onde vêm os dados.
import React from 'react';
import { Pressable, Text, View } from 'react-native';

import { Card, Sub, Titulo } from '../../components/Base';
import { PARAMETROS_PADRAO, Parametros } from '../../motor/motor';
import { cores } from '../../theme';
import { Motor, Nav } from '../estado';
import { Botao, Progresso, realce, u } from '../ui';

type Slider = { chave: keyof Parametros; rotulo: string; min: number; max: number; passo: number; fmt: (v: number) => string };

const SLIDERS: Slider[] = [
  { chave: 'pesoHistorico', rotulo: 'Peso do histórico e segmento', min: 0, max: 200, passo: 10, fmt: v => `${v}%` },
  { chave: 'bonusCalor', rotulo: 'Bônus de calor', min: 0, max: 30, passo: 1, fmt: v => `${v} pts` },
  { chave: 'limiteCalor', rotulo: 'Calor a partir de', min: 20, max: 34, passo: 1, fmt: v => `${v}°C` },
  { chave: 'bonusUmidade', rotulo: 'Bônus de hidratação (umidade)', min: 0, max: 20, passo: 1, fmt: v => `${v} pts` },
  { chave: 'bonusEvento', rotulo: 'Bônus de evento próximo', min: 0, max: 25, passo: 1, fmt: v => `${v} pts` },
  { chave: 'janelaEventos', rotulo: 'Olhar eventos dos próximos', min: 7, max: 120, passo: 7, fmt: v => `${v} dias` },
  { chave: 'metaPorCliente', rotulo: 'Meta semanal por cliente', min: 50, max: 300, passo: 10, fmt: v => `R$ ${v}` },
];

export default function Ajustes({ m, nav }: { m: Motor; nav: Nav }) {
  const set = (k: keyof Parametros, v: number) => m.setParams({ ...m.params, [k]: v });
  const mudou = SLIDERS.some(s => m.params[s.chave] !== PARAMETROS_PADRAO[s.chave]);
  return (
    <View>
      <Card style={{ marginTop: 0 }}>
        <View style={[u.linha, { gap: 10, flexWrap: 'wrap' }]}>
          <View style={{ flex: 1, minWidth: 220 }}>
            <Titulo>Parâmetros do motor</Titulo>
            <Sub>Quanto cada critério pesa na pontuação. As sugestões mudam na hora em todas as abas.</Sub>
          </View>
          {mudou && <Botao texto="Restaurar padrão" tipo="contorno" onPress={() => m.setParams(PARAMETROS_PADRAO)} />}
        </View>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', columnGap: 28 }}>
          {SLIDERS.map(s => (
            <View key={s.chave} style={{ flexBasis: nav.largo ? '45%' : '100%', flexGrow: 1 }}>
              <Ajuste s={s} valor={m.params[s.chave]} padrao={PARAMETROS_PADRAO[s.chave]} onChange={v => set(s.chave, v)} />
            </View>
          ))}
        </View>
      </Card>

      <Card>
        <Titulo>De onde vêm os dados</Titulo>
        <Text style={{ fontSize: 13, color: cores.texto, lineHeight: 20, marginTop: 6 }}>
          {m.usandoApi
            ? `Clientes e ranking vêm da API Java (motor V2): histórico real do segmento, cesta de compras ("quem compra X também compra Y"), clima (${m.api.fonteClima ?? 'simulado'}), umidade e eventos da região.`
            : 'A API está fora do ar: o app usa o motor local (V1) com dados de demonstração.'}
        </Text>
        <Text style={[u.pequeno, { marginTop: 8 }]}>
          Dados simulados para a demonstração: clientes, compras, vendas e clima. O contorno dos estados no mapa é simplificado.
        </Text>
      </Card>
    </View>
  );
}

function Ajuste({ s, valor, padrao, onChange }: { s: Slider; valor: number; padrao: number; onChange: (v: number) => void }) {
  const muda = (d: number) => onChange(Math.max(s.min, Math.min(s.max, valor + d)));
  const pct = ((valor - s.min) / (s.max - s.min)) * 100;
  return (
    <View style={{ paddingVertical: 10 }}>
      <View style={u.linha}>
        <Text style={{ flex: 1, color: cores.texto, fontSize: 13.5 }}>{s.rotulo}</Text>
        <Text style={{ color: valor === padrao ? cores.verdeMedio : cores.laranja, fontWeight: '800', fontSize: 14 }}>{s.fmt(valor)}</Text>
      </View>
      <View style={[u.linha, { marginTop: 6, gap: 10 }]}>
        <Passo texto="−" onPress={() => muda(-s.passo)} rotulo={`Diminuir ${s.rotulo}`} />
        <View style={{ flex: 1 }}>
          <Progresso pct={pct} altura={6} />
        </View>
        <Passo texto="+" onPress={() => muda(s.passo)} rotulo={`Aumentar ${s.rotulo}`} />
      </View>
    </View>
  );
}

function Passo({ texto, onPress, rotulo }: { texto: string; onPress: () => void; rotulo: string }) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityLabel={rotulo}
      style={realce({ width: 34, height: 34, borderRadius: 17, borderWidth: 1, borderColor: cores.borda, backgroundColor: '#fff', alignItems: 'center', justifyContent: 'center' })}>
      <Text style={{ fontSize: 18, color: cores.verdeEscuro, fontWeight: '700', marginTop: -2 }}>{texto}</Text>
    </Pressable>
  );
}
