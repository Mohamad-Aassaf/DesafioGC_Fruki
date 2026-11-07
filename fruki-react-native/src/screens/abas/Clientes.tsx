// Clientes: lista com busca e filtro; ao lado (ou ao tocar, no celular) as sugestões do cliente.
import React, { useState } from 'react';
import { Pressable, Text, TextInput, View } from 'react-native';

import { Card, ICONES, Icone, Sub, Titulo } from '../../components/Base';
import { Pontuacao } from '../../components/Graficos';
import { preco } from '../../data/dados';
import { classeUmidade, dicaUmidade, temperaturaMedia, umidadeMedia } from '../../motor/clima';
import { cores } from '../../theme';
import { CardCliente, Motor, Nav, normalizar } from '../estado';
import { Botao, Chip, Pontos, Selo, realce, u } from '../ui';

type Props = { m: Motor; nav: Nav; selecionado: string | null; setSelecionado: (chave: string | null) => void };

export default function Clientes({ m, nav, selecionado, setSelecionado }: Props) {
  const [busca, setBusca] = useState('');
  const [segmento, setSegmento] = useState('Todos');

  const segs = ['Todos', ...new Set(m.lista.map(c => c.segmento))];
  const seg = segs.includes(segmento) ? segmento : 'Todos';
  const q = normalizar(busca.trim());
  const visiveis = m.cards.filter(
    x => (seg === 'Todos' || x.c.segmento === seg) && normalizar(`${x.c.nome} ${x.c.cidade} ${x.c.uf}`).includes(q),
  );
  const atual = m.cards.find(x => x.chave === selecionado) ?? (nav.largo ? visiveis[0] : undefined);

  const lista = (
    <View>
      <View style={[u.linha, { backgroundColor: '#fff', borderWidth: 1, borderColor: cores.borda, borderRadius: 12, paddingHorizontal: 12 }]}>
        <Icone d={ICONES.busca} cor={cores.mut} tamanho={20} />
        <TextInput
          value={busca}
          onChangeText={setBusca}
          placeholder="Buscar cliente, cidade ou UF"
          placeholderTextColor={cores.mut}
          style={{ flex: 1, height: 44, marginLeft: 8, color: cores.texto, fontSize: 14 }}
        />
      </View>
      <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 10 }}>
        {segs.map(s => (
          <Chip
            key={s}
            ativo={s === seg}
            onPress={() => setSegmento(s)}
            texto={s}
            contador={s === 'Todos' ? m.lista.length : m.lista.filter(c => c.segmento === s).length}
          />
        ))}
      </View>
      <Text style={[u.pequeno, { marginTop: 10, marginBottom: 6 }]}>
        {visiveis.length} de {m.lista.length} clientes · {m.visitas} visitados hoje
      </Text>
      {visiveis.length === 0 && <Text style={u.pequeno}>Nenhum cliente encontrado.</Text>}
      {visiveis.map(x => (
        <ItemCliente
          key={x.chave}
          x={x}
          ativo={nav.largo && atual?.chave === x.chave}
          visitado={!!m.visitados[x.chave]}
          onPress={() => setSelecionado(x.chave)}
        />
      ))}
    </View>
  );

  if (!nav.largo) {
    return atual ? (
      <View>
        <Pressable onPress={() => setSelecionado(null)} style={[u.linha, { gap: 4, marginBottom: 4 }]} accessibilityRole="button">
          <Icone d={ICONES.esquerda} cor={cores.verde} tamanho={22} />
          <Text style={{ color: cores.verde, fontWeight: '700' }}>Todos os clientes</Text>
        </Pressable>
        <Detalhe x={atual} m={m} nav={nav} />
      </View>
    ) : (
      lista
    );
  }

  return (
    <View style={{ flexDirection: 'row', gap: 16, alignItems: 'flex-start' }}>
      <View style={{ width: 360 }}>{lista}</View>
      <View style={{ flex: 1, minWidth: 0 }}>
        {atual ? <Detalhe x={atual} m={m} nav={nav} /> : <Text style={u.pequeno}>Escolha um cliente.</Text>}
      </View>
    </View>
  );
}

function ItemCliente({ x, ativo, visitado, onPress }: { x: CardCliente; ativo: boolean; visitado: boolean; onPress: () => void }) {
  const r = x.rows[0];
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ selected: ativo }}
      style={realce(
        {
          backgroundColor: ativo ? cores.verdeClaro : '#fff',
          borderWidth: ativo ? 2 : 1,
          borderColor: ativo ? cores.verde : cores.borda,
          borderRadius: 14,
          padding: 12,
          marginBottom: 8,
        },
        { borderColor: cores.verde },
      )}>
      <View style={[u.linha, { gap: 8 }]}>
        <Text style={[u.forte, { flex: 1 }]} numberOfLines={1}>
          {x.c.nome}
        </Text>
        {visitado && <Selo texto="✓ visitado" />}
        <Selo texto={x.c.segmento} cor={cores.mut} fundo={cores.app} />
      </View>
      <Text style={[u.pequeno, { marginTop: 2 }]}>
        {x.c.cidade} - {x.c.uf} · {x.c.mix.length} produtos no mix
      </Text>
      <View style={[u.linha, { marginTop: 6, gap: 6 }]}>
        {x.carregando ? (
          <Text style={u.pequeno}>Carregando sugestões…</Text>
        ) : r ? (
          <>
            <Text style={{ fontSize: 13, color: cores.verdeMedio, fontWeight: '700', flex: 1 }} numberOfLines={1}>
              ★ {r.produto.nome}
              {x.rows.length > 1 ? <Text style={{ color: cores.mut, fontWeight: '400' }}>{` +${x.rows.length - 1}`}</Text> : null}
            </Text>
            <Text style={[u.pequeno, { fontWeight: '700' }]}>R$ {x.potencial}</Text>
          </>
        ) : (
          <Text style={u.pequeno}>Sem sugestão agora</Text>
        )}
      </View>
    </Pressable>
  );
}

function Detalhe({ x, m, nav }: { x: CardCliente; m: Motor; nav: Nav }) {
  const { c, rk, ids, rows, avisos, carregando } = x;
  const t = temperaturaMedia(c.uf);
  const um = umidadeMedia(c.uf);
  const cl = classeUmidade(um);
  const visitado = !!m.visitados[x.chave];
  return (
    <View>
      <Card style={{ marginTop: nav.largo ? 0 : 8 }}>
        <View style={[u.linha, { gap: 12, flexWrap: 'wrap' }]}>
          <View style={{ width: 48, height: 48, borderRadius: 14, backgroundColor: cores.verdeClaro, alignItems: 'center', justifyContent: 'center' }}>
            <Text style={{ fontSize: 20, fontWeight: '800', color: cores.verdeEscuro }}>{c.nome.charAt(0)}</Text>
          </View>
          <View style={{ flex: 1, minWidth: 180 }}>
            <Text style={{ fontSize: 20, fontWeight: '800', color: cores.verdeEscuro }}>{c.nome}</Text>
            <View style={[u.linha, { gap: 6, marginTop: 4, flexWrap: 'wrap' }]}>
              <Selo texto={c.segmento} />
              <Selo texto={`${c.cidade} - ${c.uf}`} cor={cores.mut} fundo={cores.app} />
              <Selo texto={`${t}°C`} cor={cores.laranja} fundo={cores.laranjaClaro} />
              <Selo texto={`umidade ${um}%`} cor={cores.azul} fundo={cores.azulClaro} />
            </View>
          </View>
          <Botao
            texto={visitado ? 'Visitado' : 'Marcar visita'}
            icone={ICONES.check}
            tipo={visitado ? 'primario' : 'contorno'}
            onPress={() => m.alternarVisita(x.chave)}
          />
        </View>

        <Text style={{ fontSize: 13, fontWeight: '800', color: cores.verde, marginTop: 18, marginBottom: 6, letterSpacing: 0.5 }}>
          OFERECER AGORA
        </Text>
        {cl && (
          <Aviso texto={`Umidade ${cl} (${um}%): ${dicaUmidade(cl)}`} />
        )}
        {avisos.map(a => (
          <Aviso key={a} texto={a} />
        ))}
        {carregando && <Text style={u.pequeno}>Carregando sugestões da API…</Text>}
        {!carregando && rows.length === 0 && <Text style={u.pequeno}>Nenhuma sugestão para este cliente agora.</Text>}
        {rows.map((r, sIdx) => (
          <View key={r.produto.id} style={{ borderWidth: 1, borderColor: cores.borda, borderRadius: 14, padding: 12, marginTop: 8 }}>
            <View style={[u.linha, { gap: 10 }]}>
              <Text style={{ width: 26, height: 26, lineHeight: 26, borderRadius: 13, textAlign: 'center', backgroundColor: cores.verdeEscuro, color: '#fff', fontWeight: '800', overflow: 'hidden' }}>
                {sIdx + 1}
              </Text>
              <View style={{ flex: 1 }}>
                <Text style={{ fontSize: 16, fontWeight: '700', color: cores.texto }}>{r.produto.nome}</Text>
                <Text style={u.pequeno}>
                  {r.produto.familia} · R$ {preco(r.produto)}/cx
                  {ids[sIdx] !== rk[sIdx]?.produto.id && <Text style={{ color: cores.laranja, fontWeight: '700' }}> · ajustado por você</Text>}
                </Text>
              </View>
              <Pontos valor={r.pontos} />
              <Pressable
                onPress={() => nav.pedirTroca(x.chave, sIdx)}
                accessibilityLabel={`Trocar a sugestão ${r.produto.nome}`}
                style={realce({ padding: 6, borderRadius: 10, borderWidth: 1, borderColor: cores.borda })}>
                <Icone d={ICONES.troca} cor={cores.verdeEscuro} tamanho={18} />
              </Pressable>
            </View>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 8 }}>
              {r.motivos.map(mo => (
                <Text key={mo} style={{ fontSize: 12, color: cores.texto, backgroundColor: cores.app, borderRadius: 8, paddingHorizontal: 8, paddingVertical: 3 }}>
                  {mo}
                </Text>
              ))}
            </View>
          </View>
        ))}
        {rows.length > 0 && (
          <Botao texto="Enviar pelo Pede Fruki (WhatsApp)" icone={ICONES.chat} onPress={() => nav.enviarWhats(x)} style={{ marginTop: 14 }} />
        )}
      </Card>

      {rows.length > 0 && (
        <Card>
          <Titulo>Por que essas sugestões?</Titulo>
          <Sub>Quanto cada critério somou na pontuação de cada produto.</Sub>
          <Pontuacao itens={rows} />
        </Card>
      )}

      <Card>
        <Titulo>O cliente já compra</Titulo>
        <Sub>{c.mix.length ? `${c.mix.length} produtos no histórico. Estes ficam fora das sugestões.` : 'Cliente novo, ainda sem compras registradas.'}</Sub>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 6 }}>
          {c.mix.map(id => (
            <Selo key={id} texto={m.nomeProduto(id)} cor={cores.texto} fundo={cores.app} />
          ))}
        </View>
      </Card>
    </View>
  );
}

function Aviso({ texto }: { texto: string }) {
  return (
    <View style={{ flexDirection: 'row', gap: 8, backgroundColor: cores.laranjaClaro, borderRadius: 10, padding: 10, marginBottom: 6 }}>
      <Text style={{ color: cores.laranja, fontWeight: '800' }}>!</Text>
      <Text style={{ flex: 1, fontSize: 12.5, color: '#8A4A12' }}>{texto}</Text>
    </View>
  );
}
