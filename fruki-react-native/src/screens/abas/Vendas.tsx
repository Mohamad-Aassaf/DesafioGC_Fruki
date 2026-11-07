// Vendas: volume por família (toque para ver os produtos) e calendário de datas comerciais.
import React, { useState } from 'react';
import { Text, View } from 'react-native';

import { Card, ICONES, Icone, Sub, Titulo } from '../../components/Base';
import { Barras } from '../../components/Graficos';
import { FAMILIAS } from '../../data/dados';
import { dias, eventosFuturos } from '../../motor/motor';
import { cores } from '../../theme';
import { Nav } from '../estado';
import { Botao, Colunas, Selo, u } from '../ui';

export default function Vendas({ nav }: { nav: Nav }) {
  const [familia, setFamilia] = useState<string | null>(null);
  const vendas: [string, number][] = (
    familia
      ? Object.entries(FAMILIAS.find(f => f.nome === familia)!.produtos)
      : FAMILIAS.map(f => [f.nome, f.volume] as [string, number])
  ).sort((a, b) => b[1] - a[1]);
  const mes = (d: Date) => d.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '').toUpperCase();

  return (
    <Colunas largo={nav.largo} proporcao={[3, 2]}>
      <Card style={{ marginTop: nav.largo ? 0 : 16 }}>
        <Titulo>{familia ? `Produtos: ${familia}` : 'Vendas por família de produtos'}</Titulo>
        <Sub>
          Do mais para o menos vendido, em % do total de 12 meses.
          {familia ? '' : ' Toque em uma coluna para ver os produtos.'}
        </Sub>
        {familia && (
          <Botao texto="Voltar às famílias" icone={ICONES.esquerda} tipo="contorno" onPress={() => setFamilia(null)} style={{ alignSelf: 'flex-start', marginBottom: 6 }} />
        )}
        <Barras itens={vendas} onBarra={familia ? undefined : setFamilia} />
        <View style={{ flexDirection: 'row', gap: 10, marginTop: 12 }}>
          <Destaque cor={cores.verde} rotulo="Mais vendido" item={vendas[0]} />
          <Destaque cor={cores.laranja} rotulo="Menos vendido: espaço para crescer" item={vendas[vendas.length - 1]} />
        </View>
      </Card>

      <Card style={{ marginTop: nav.largo ? 0 : 16 }}>
        <Titulo>Calendário comercial</Titulo>
        <Sub>Datas que puxam vendas. Em laranja: faltam até 14 dias.</Sub>
        {eventosFuturos().map(e => {
          const d = dias(e);
          const breve = d <= 14;
          return (
            <View
              key={e.nome}
              style={[u.linha, { gap: 12, paddingVertical: 10, borderTopWidth: 1, borderTopColor: cores.borda }]}>
              <View style={{ width: 50, alignItems: 'center', backgroundColor: breve ? cores.laranjaClaro : cores.app, borderRadius: 10, paddingVertical: 6 }}>
                <Text style={{ fontSize: 11, color: breve ? cores.laranja : cores.mut, fontWeight: '700' }}>
                  {mes(e.data)}
                </Text>
                <Text style={{ fontSize: 18, fontWeight: '800', color: breve ? cores.laranja : cores.texto }}>{e.data.getDate()}</Text>
              </View>
              <View style={{ flex: 1 }}>
                <Text style={{ fontSize: 14, fontWeight: '700', color: cores.texto }}>{e.nome}</Text>
                <Text style={u.pequeno}>
                  {e.abrangencia}
                  {e.familias.length ? ` · puxa ${e.familias.join(', ').toLowerCase()}` : ''}
                </Text>
              </View>
              <Selo texto={d === 0 ? 'hoje' : `${d} dias`} cor={breve ? cores.laranja : cores.mut} fundo={breve ? cores.laranjaClaro : cores.app} />
            </View>
          );
        })}
        <View style={[u.linha, { gap: 6, marginTop: 8 }]}>
          <Icone d={ICONES.calendario} cor={cores.mut} tamanho={16} />
          <Text style={u.pequeno}>Datas próximas dão bônus às famílias que elas puxam.</Text>
        </View>
      </Card>
    </Colunas>
  );
}

function Destaque({ cor, rotulo, item }: { cor: string; rotulo: string; item: [string, number] }) {
  return (
    <View style={{ flex: 1, backgroundColor: cores.app, borderRadius: 12, padding: 12, borderLeftWidth: 4, borderLeftColor: cor }}>
      <Text style={u.pequeno}>{rotulo}</Text>
      <Text style={u.forte}>{item[0]}</Text>
      <Text style={u.pequeno}>{item[1]} mil un.</Text>
    </View>
  );
}
