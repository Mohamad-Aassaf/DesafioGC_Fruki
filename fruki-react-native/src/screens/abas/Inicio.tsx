// Início: resumo do dia do vendedor, por onde começar, clima e próximos eventos.
import React from 'react';
import { Pressable, Text, View } from 'react-native';

import { Card, ICONES, Icone, Sub, Titulo } from '../../components/Base';
import { NOME_UF } from '../../data/dados';
import { classeUmidade, dicaUmidade, temperaturaMedia, umidadeMedia } from '../../motor/clima';
import { dias, eventosFuturos } from '../../motor/motor';
import { cores } from '../../theme';
import { Motor, Nav } from '../estado';
import { Colunas, Kpi, Pontos, Progresso, Selo, realce, u } from '../ui';

const saudacao = () => {
  const h = new Date().getHours();
  return h < 12 ? 'Bom dia' : h < 18 ? 'Boa tarde' : 'Boa noite';
};

/** UF com mais clientes na carteira (referência do clima do dia). */
function ufPrincipal(ufs: string[]) {
  const n: Record<string, number> = {};
  ufs.forEach(x => (n[x] = (n[x] ?? 0) + 1));
  return Object.entries(n).sort((a, b) => b[1] - a[1])[0]?.[0] ?? 'RS';
}

export default function Inicio({ m, nav }: { m: Motor; nav: Nav }) {
  const hoje = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
  const prioridades = [...m.cards]
    .filter(x => x.rows.length > 0)
    .sort((a, b) => Number(!!m.visitados[a.chave]) - Number(!!m.visitados[b.chave]) || (b.rows[0]?.pontos ?? 0) - (a.rows[0]?.pontos ?? 0))
    .slice(0, 5);
  const uf = ufPrincipal(m.lista.map(c => c.uf));
  const t = temperaturaMedia(uf);
  const um = umidadeMedia(uf);
  const cu = classeUmidade(um);
  const evs = eventosFuturos().slice(0, 4);
  const pctVisitas = m.lista.length ? (m.visitas / m.lista.length) * 100 : 0;

  return (
    <View>
      <Text style={{ fontSize: 24, fontWeight: '800', color: cores.verdeEscuro }}>{saudacao()}! 👋</Text>
      <Text style={[u.pequeno, { fontSize: 14, marginTop: 2 }]}>
        {hoje.charAt(0).toUpperCase() + hoje.slice(1)} · carteira de {m.vendedor}
      </Text>

      <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 12, marginTop: 16 }}>
        <Kpi icone={ICONES.clientes} rotulo="Visitas de hoje" valor={`${m.visitas} de ${m.lista.length}`}>
          <View style={{ marginTop: 6 }}>
            <Progresso pct={pctVisitas} altura={6} />
          </View>
        </Kpi>
        <Kpi
          icone={ICONES.vendas}
          rotulo="Meta da semana (itens novos)"
          valor={`${m.pct}%`}
          detalhe={`R$ ${m.total.toLocaleString('pt-BR')} de R$ ${m.meta.toLocaleString('pt-BR')}`}
          cor={cores.laranja}
        />
        <Kpi
          icone={ICONES.check}
          rotulo="Potencial por cliente"
          valor={`R$ ${Math.round(m.total / Math.max(1, m.lista.length))}`}
          detalhe="1 caixa de cada sugestão"
          cor={cores.azul}
        />
        <Kpi icone={ICONES.mundo} rotulo="SKUs novos para oferecer" valor={String(m.skusNovos)} detalhe="produtos que os clientes ainda não compram" cor="#7A5AF8" />
      </View>

      <Colunas largo={nav.largo}>
        <Card>
          <Titulo>Comece por aqui</Titulo>
          <Sub>Clientes com as sugestões mais fortes agora. Os já visitados vão para o fim da lista.</Sub>
          {prioridades.length === 0 && <Text style={u.pequeno}>Carregando sugestões…</Text>}
          {prioridades.map((x, i) => {
            const r = x.rows[0];
            const visitado = !!m.visitados[x.chave];
            return (
              <Pressable
                key={x.chave}
                onPress={() => nav.abrirCliente(m.vendedor, x.c)}
                accessibilityRole="button"
                accessibilityLabel={`Abrir ${x.c.nome}`}
                style={realce({ flexDirection: 'row', alignItems: 'center', gap: 12, padding: 10, borderRadius: 12, opacity: visitado ? 0.6 : 1 })}>
                <View style={{ width: 30, height: 30, borderRadius: 15, backgroundColor: visitado ? cores.verde : cores.verdeClaro, alignItems: 'center', justifyContent: 'center' }}>
                  {visitado ? (
                    <Icone d={ICONES.check} cor="#fff" tamanho={18} />
                  ) : (
                    <Text style={{ fontWeight: '800', color: cores.verdeEscuro }}>{i + 1}</Text>
                  )}
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={u.forte}>{x.c.nome}</Text>
                  <Text style={u.pequeno} numberOfLines={1}>
                    {x.c.cidade} - {x.c.uf} · oferecer <Text style={{ color: cores.verdeMedio, fontWeight: '700' }}>{r.produto.nome}</Text>
                    {r.motivos[0] ? ` · ${r.motivos[0]}` : ''}
                  </Text>
                </View>
                <Pontos valor={r.pontos} />
                <Icone d={ICONES.direita} cor={cores.mut} tamanho={20} />
              </Pressable>
            );
          })}
          <Pressable onPress={() => nav.irPara('clientes')} style={{ marginTop: 8, alignSelf: 'flex-start' }}>
            <Text style={{ color: cores.verde, fontWeight: '700' }}>Ver todos os {m.lista.length} clientes →</Text>
          </Pressable>
        </Card>

        <View>
          <Card>
            <View style={u.linha}>
              <View style={{ flex: 1 }}>
                <Titulo>Clima em {NOME_UF[uf] ?? uf}</Titulo>
                <Text style={u.pequeno}>Média dos próximos 14 dias</Text>
              </View>
              <Pressable onPress={() => nav.irPara('mapa')}>
                <Text style={{ color: cores.verde, fontWeight: '700' }}>Ver mapa →</Text>
              </Pressable>
            </View>
            <View style={{ flexDirection: 'row', gap: 10, marginTop: 12 }}>
              <View style={{ flex: 1, backgroundColor: cores.laranjaClaro, borderRadius: 12, padding: 12 }}>
                <Icone d={ICONES.sol} cor={cores.laranja} tamanho={20} />
                <Text style={{ fontSize: 22, fontWeight: '800', color: cores.texto, marginTop: 4 }}>{t}°C</Text>
                <Text style={u.pequeno}>máxima média</Text>
              </View>
              <View style={{ flex: 1, backgroundColor: cores.azulClaro, borderRadius: 12, padding: 12 }}>
                <Icone d={ICONES.gota} cor={cores.azul} tamanho={20} />
                <Text style={{ fontSize: 22, fontWeight: '800', color: cores.texto, marginTop: 4 }}>{um}%</Text>
                <Text style={u.pequeno}>umidade</Text>
              </View>
            </View>
            <Text style={[u.pequeno, { marginTop: 10, color: cores.texto, fontSize: 13 }]}>
              {t >= m.params.limiteCalor
                ? 'Calor: bom momento para bebidas geladas.'
                : t <= 22
                  ? 'Mais frio: aposte em chás e energéticos.'
                  : 'Temperatura amena.'}
              {cu ? ` Umidade ${cu}: ${dicaUmidade(cu)}` : ''}
            </Text>
          </Card>

          <Card>
            <Titulo>Próximas datas</Titulo>
            <Sub>Em laranja: faltam até 14 dias.</Sub>
            {evs.map(e => {
              const d = dias(e);
              const breve = d <= 14;
              return (
                <View key={e.nome} style={[u.linha, { gap: 10, paddingVertical: 8, borderTopWidth: 1, borderTopColor: cores.borda }]}>
                  <Icone d={ICONES.calendario} cor={breve ? cores.laranja : cores.mut} tamanho={18} />
                  <View style={{ flex: 1 }}>
                    <Text style={{ fontSize: 14, color: cores.texto, fontWeight: '600' }}>{e.nome}</Text>
                    <Text style={u.pequeno} numberOfLines={1}>
                      {e.familias.length ? `puxa ${e.familias.join(', ').toLowerCase()}` : e.abrangencia}
                    </Text>
                  </View>
                  <Selo
                    texto={d === 0 ? 'hoje' : `em ${d} dias`}
                    cor={breve ? cores.laranja : cores.mut}
                    fundo={breve ? cores.laranjaClaro : cores.app}
                  />
                </View>
              );
            })}
          </Card>
        </View>
      </Colunas>
    </View>
  );
}
