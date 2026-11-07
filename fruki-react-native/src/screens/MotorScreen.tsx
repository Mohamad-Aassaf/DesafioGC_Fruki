// App do vendedor: navegação por abas (menu lateral na tela larga, barra inferior no celular).
import React, { useState } from 'react';
import { Modal, Pressable, ScrollView, Text, View, useWindowDimensions } from 'react-native';

import { ICONES, Icone, Logo } from '../components/Base';
import { Cliente, preco } from '../data/dados';
import { cores } from '../theme';
import Ajustes from './abas/Ajustes';
import Clientes from './abas/Clientes';
import Inicio from './abas/Inicio';
import Mapa from './abas/Mapa';
import Vendas from './abas/Vendas';
import { Aba, CardCliente, Nav, chaveCliente, useMotor } from './estado';
import { Conteudo, realce, u } from './ui';

const ABAS: { id: Aba; nome: string; icone: string; titulo: string }[] = [
  { id: 'inicio', nome: 'Início', icone: ICONES.inicio, titulo: 'Resumo do dia' },
  { id: 'clientes', nome: 'Clientes', icone: ICONES.clientes, titulo: 'Clientes e sugestões' },
  { id: 'mapa', nome: 'Mapa', icone: ICONES.mapa, titulo: 'Regiões e clima' },
  { id: 'vendas', nome: 'Vendas', icone: ICONES.vendas, titulo: 'Vendas e datas' },
  { id: 'ajustes', nome: 'Ajustes', icone: ICONES.ajustes, titulo: 'Ajustes do motor' },
];

export default function MotorScreen() {
  const { width } = useWindowDimensions();
  const largo = width >= 900;
  const m = useMotor();
  const [aba, setAba] = useState<Aba>('inicio');
  const [selecionado, setSelecionado] = useState<string | null>(null);
  const [troca, setTroca] = useState<{ chave: string; slot: number } | null>(null);
  const [whats, setWhats] = useState<CardCliente | null>(null);
  const [escolherVendedor, setEscolherVendedor] = useState(false);

  const nav: Nav = {
    largo,
    irPara: a => {
      if (a === 'clientes') setSelecionado(null);
      setAba(a);
    },
    abrirCliente: (vendedor: string, c: Cliente) => {
      m.setVendedor(vendedor);
      setSelecionado(chaveCliente(vendedor, c));
      setAba('clientes');
    },
    pedirTroca: (chave, slot) => setTroca({ chave, slot }),
    enviarWhats: card => setWhats(card),
  };

  const trocarVendedor = (v: string) => {
    m.setVendedor(v);
    setSelecionado(null);
    setEscolherVendedor(false);
  };

  const cardTroca = troca ? m.cards.find(x => x.chave === troca.chave) : undefined;
  const info = ABAS.find(a => a.id === aba)!;

  const status = (
    <View style={[u.linha, { gap: 6 }]}>
      <View
        style={{
          width: 8,
          height: 8,
          borderRadius: 4,
          backgroundColor: m.usandoApi ? '#3DDC84' : m.api.online === false ? cores.amarelo : '#AAB',
        }}
      />
      <Text style={{ fontSize: 12, color: largo ? '#CFE3D8' : cores.mut }} numberOfLines={2}>
        {m.api.online === null ? 'Conectando à API…' : m.usandoApi ? 'API Java · motor V2' : 'Sem API: motor local'}
      </Text>
    </View>
  );

  const seletorVendedor = (
    <Pressable
      onPress={() => setEscolherVendedor(true)}
      accessibilityRole="button"
      accessibilityLabel={`Vendedor: ${m.vendedor}. Trocar`}
      style={realce([u.linha, { gap: 8, borderWidth: 1, borderColor: cores.borda, borderRadius: 22, paddingVertical: 7, paddingLeft: 7, paddingRight: 12, backgroundColor: '#fff' }], {
        borderColor: cores.verde,
      })}>
      <View style={{ width: 28, height: 28, borderRadius: 14, backgroundColor: cores.verdeEscuro, alignItems: 'center', justifyContent: 'center' }}>
        <Icone d={ICONES.clientes} cor="#fff" tamanho={16} />
      </View>
      <View style={{ maxWidth: largo ? 260 : 170 }}>
        <Text style={{ fontSize: 10, color: cores.mut }}>Carteira</Text>
        <Text style={{ fontSize: 13, fontWeight: '700', color: cores.texto }} numberOfLines={1}>
          {m.vendedor}
        </Text>
      </View>
      <Text style={{ color: cores.mut }}>▾</Text>
    </Pressable>
  );

  const pagina = (
    <ScrollView
      key={aba}
      style={{ flex: 1 }}
      contentContainerStyle={{ padding: largo ? 28 : 14, paddingBottom: 40 }}
      keyboardShouldPersistTaps="handled">
      <Conteudo>
        {aba === 'inicio' && <Inicio m={m} nav={nav} />}
        {aba === 'clientes' && <Clientes m={m} nav={nav} selecionado={selecionado} setSelecionado={setSelecionado} />}
        {aba === 'mapa' && <Mapa m={m} nav={nav} />}
        {aba === 'vendas' && <Vendas nav={nav} />}
        {aba === 'ajustes' && <Ajustes m={m} nav={nav} />}
      </Conteudo>
    </ScrollView>
  );

  return (
    <View style={{ flex: 1, flexDirection: largo ? 'row' : 'column', backgroundColor: cores.app }}>
      {largo ? (
        <View style={{ width: 232, backgroundColor: cores.verdeEscuro, paddingVertical: 20, paddingHorizontal: 14 }}>
          <View style={[u.linha, { gap: 10, paddingHorizontal: 6, marginBottom: 26 }]}>
            <View style={{ backgroundColor: '#fff', borderRadius: 10, padding: 4 }}>
              <Logo tamanho={26} />
            </View>
            <View>
              <Text style={{ color: '#fff', fontSize: 17, fontWeight: '800' }}>Fruki</Text>
              <Text style={{ color: '#9FC7B2', fontSize: 12 }}>App do vendedor</Text>
            </View>
          </View>
          {ABAS.map(a => {
            const ativo = a.id === aba;
            return (
              <Pressable
                key={a.id}
                onPress={() => nav.irPara(a.id)}
                accessibilityRole="tab"
                accessibilityState={{ selected: ativo }}
                style={realce(
                  [u.linha, { gap: 12, paddingVertical: 11, paddingHorizontal: 12, borderRadius: 12, marginBottom: 4 }, ativo && { backgroundColor: cores.verde }],
                  ativo ? {} : { backgroundColor: 'rgba(255,255,255,0.08)' },
                )}>
                <Icone d={a.icone} cor={ativo ? '#fff' : '#B9D6C7'} tamanho={20} />
                <Text style={{ color: ativo ? '#fff' : '#DCEBE3', fontWeight: ativo ? '700' : '500', fontSize: 14 }}>{a.nome}</Text>
              </Pressable>
            );
          })}
          <View style={{ flex: 1 }} />
          <View style={{ padding: 10, borderRadius: 12, backgroundColor: 'rgba(255,255,255,0.06)' }}>{status}</View>
        </View>
      ) : null}

      <View style={{ flex: 1 }}>
        {/* Cabeçalho */}
        <View
          style={[
            u.linha,
            {
              gap: 10,
              paddingHorizontal: largo ? 28 : 14,
              paddingVertical: largo ? 14 : 10,
              backgroundColor: '#fff',
              borderBottomWidth: 1,
              borderBottomColor: cores.borda,
            },
          ]}>
          {!largo && <Logo tamanho={26} />}
          <View style={{ flex: 1 }}>
            <Text style={{ fontSize: largo ? 20 : 17, fontWeight: '800', color: cores.verdeEscuro }}>{largo ? info.titulo : info.nome}</Text>
            {!largo && status}
          </View>
          {seletorVendedor}
        </View>
        {m.api.online === false && (
          <Text style={{ fontSize: 12, color: '#8A5A00', backgroundColor: '#FFF6DB', paddingHorizontal: largo ? 28 : 14, paddingVertical: 6 }}>
            API fora do ar: usando o motor local (V1) com dados de demonstração.
          </Text>
        )}

        {pagina}

        {!largo && (
          <View style={{ flexDirection: 'row', backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: cores.borda, paddingVertical: 6 }}>
            {ABAS.map(a => {
              const ativo = a.id === aba;
              return (
                <Pressable
                  key={a.id}
                  onPress={() => nav.irPara(a.id)}
                  accessibilityRole="tab"
                  accessibilityState={{ selected: ativo }}
                  style={{ flex: 1, alignItems: 'center', paddingVertical: 4 }}>
                  <View style={{ paddingHorizontal: 16, paddingVertical: 4, borderRadius: 16, backgroundColor: ativo ? cores.verdeClaro : 'transparent' }}>
                    <Icone d={a.icone} cor={ativo ? cores.verde : cores.mut} tamanho={22} />
                  </View>
                  <Text style={{ fontSize: 11, marginTop: 2, color: ativo ? cores.verdeEscuro : cores.mut, fontWeight: ativo ? '700' : '500' }}>
                    {a.nome}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        )}
      </View>

      {/* Escolha da carteira (vendedor) */}
      <Modal visible={escolherVendedor} transparent animationType="fade" onRequestClose={() => setEscolherVendedor(false)}>
        <Pressable style={veu(largo)} onPress={() => setEscolherVendedor(false)}>
          <Pressable onPress={() => undefined} style={folha(largo)}>
            <Text style={[u.forte, { marginBottom: 8 }]}>Escolher carteira</Text>
            {m.vendedores.map(v => {
              const ativo = v === m.vendedor;
              const n = m.fonte[v]?.length ?? 0;
              return (
                <Pressable
                  key={v}
                  onPress={() => trocarVendedor(v)}
                  style={realce([u.linha, { gap: 10, paddingVertical: 12, paddingHorizontal: 10, borderRadius: 10 }, ativo && { backgroundColor: cores.verdeClaro }])}>
                  <Text style={{ flex: 1, color: cores.texto, fontWeight: ativo ? '700' : '500' }}>{v}</Text>
                  <Text style={u.pequeno}>
                    {n} {n === 1 ? 'cliente' : 'clientes'}
                  </Text>
                </Pressable>
              );
            })}
          </Pressable>
        </Pressable>
      </Modal>

      {/* Troca de sugestão */}
      <Modal visible={!!cardTroca} transparent animationType="fade" onRequestClose={() => setTroca(null)}>
        <Pressable style={veu(largo)} onPress={() => setTroca(null)}>
          <Pressable onPress={() => undefined} style={folha(largo)}>
            <Text style={[u.forte, { marginBottom: 2 }]}>Trocar sugestão</Text>
            <Text style={[u.pequeno, { marginBottom: 8 }]}>Outros produtos que o cliente ainda não compra, por pontuação.</Text>
            {troca &&
              cardTroca &&
              cardTroca.rk
                .filter(x => x.produto.id === cardTroca.ids[troca.slot] || !cardTroca.ids.includes(x.produto.id))
                .map(x => {
                  const atual = x.produto.id === cardTroca.ids[troca.slot];
                  return (
                    <Pressable
                      key={x.produto.id}
                      style={realce([u.linha, { paddingVertical: 11, paddingHorizontal: 10, borderRadius: 10 }, atual && { backgroundColor: cores.verdeClaro }])}
                      onPress={() => {
                        m.trocarSugestao(troca.chave, troca.slot, x.produto.id);
                        setTroca(null);
                      }}>
                      <Text style={{ flex: 1, color: cores.texto, fontWeight: atual ? '700' : '400' }}>{x.produto.nome}</Text>
                      <Text style={[u.pequeno, { marginRight: 10 }]}>R$ {preco(x.produto)}/cx</Text>
                      <Text style={{ color: cores.verde, fontWeight: '800' }}>{x.pontos} pts</Text>
                    </Pressable>
                  );
                })}
          </Pressable>
        </Pressable>
      </Modal>

      {/* Prévia WhatsApp */}
      <Modal visible={!!whats} transparent animationType="fade" onRequestClose={() => setWhats(null)}>
        <Pressable style={veu(largo)} onPress={() => setWhats(null)}>
          <Pressable onPress={() => undefined} style={[folha(largo), { backgroundColor: cores.whatsFundo }]}>
            <Text style={[u.forte, { marginBottom: 10 }]}>Prévia no WhatsApp · Pede Fruki</Text>
            {whats && (
              <View style={{ backgroundColor: cores.whatsBalao, borderRadius: 12, borderTopLeftRadius: 2, padding: 12, marginRight: 40 }}>
                <Text style={{ color: cores.texto, fontSize: 14, lineHeight: 20 }}>
                  {`Olá, ${whats.c.nome}! 👋\nAqui é o Pede Fruki. Separamos sugestões para a sua loja nesta semana:\n\n` +
                    whats.rows
                      .map((r, i) => `${i + 1}. ${r.produto.nome} — R$ ${preco(r.produto)}/cx` + (r.motivos[1] ? `\n   ${r.motivos[1]}` : ''))
                      .join('\n') +
                    '\n\nResponda com o número do item para incluir no seu pedido. 🥤'}
                </Text>
              </View>
            )}
            <Pressable
              style={realce([u.linha, { justifyContent: 'center', marginTop: 14, backgroundColor: cores.verde, borderRadius: 12, paddingVertical: 12 }], {
                backgroundColor: cores.verdeMedio,
              })}
              onPress={() => setWhats(null)}>
              <Text style={{ color: '#fff', fontWeight: '700' }}>Fechar</Text>
            </Pressable>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const veu = (largo: boolean) => ({
  flex: 1,
  backgroundColor: 'rgba(11,59,44,0.35)',
  justifyContent: largo ? ('center' as const) : ('flex-end' as const),
  alignItems: 'center' as const,
});

const folha = (largo: boolean) => ({
  backgroundColor: '#fff',
  width: '100%' as const,
  maxWidth: largo ? 460 : undefined,
  borderRadius: largo ? 18 : 0,
  borderTopLeftRadius: 20,
  borderTopRightRadius: 20,
  padding: 18,
  paddingBottom: 26,
});
