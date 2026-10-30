// Motor de recomendação: parâmetros, clima, clientes com sugestões, vendas e eventos.
import React, { useMemo, useState } from 'react';
import { Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { Card, ICONES, Icone, Logo, Sub, Titulo } from '../components/Base';
import { Barras, ClimaGrafico, MapaBrasil, Pontuacao } from '../components/Graficos';
import { Cliente, FAMILIAS, VENDEDORES, preco } from '../data/dados';
import { classeUmidade, dicaUmidade, previsao, temperaturaMedia, umidadeMedia } from '../motor/clima';
import { PARAMETROS_PADRAO, Parametros, Recomendacao, dias, eventosFuturos, ranking } from '../motor/motor';
import { cores } from '../theme';

const SUGESTOES = 3;
const VEND = Object.keys(VENDEDORES);

const normalizar = (s: string) =>
  s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

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

/** Controle de parâmetro com botões − / + e barra de posição. */
function Ajuste({ s, valor, onChange }: { s: Slider; valor: number; onChange: (v: number) => void }) {
  const muda = (d: number) => onChange(Math.max(s.min, Math.min(s.max, valor + d)));
  const pct = ((valor - s.min) / (s.max - s.min)) * 100;
  return (
    <View style={m.ajuste}>
      <View style={m.linha}>
        <Text style={{ flex: 1, color: cores.texto, fontSize: 13 }}>{s.rotulo}</Text>
        <Text style={{ color: cores.verdeMedio, fontWeight: '700', fontSize: 13 }}>{s.fmt(valor)}</Text>
      </View>
      <View style={[m.linha, { marginTop: 6 }]}>
        <Pressable style={m.passo} onPress={() => muda(-s.passo)} accessibilityLabel={`Diminuir ${s.rotulo}`}>
          <Text style={m.passoTxt}>−</Text>
        </Pressable>
        <View style={m.trilho}>
          <View style={[m.trilhoCheio, { width: `${pct}%` }]} />
        </View>
        <Pressable style={m.passo} onPress={() => muda(s.passo)} accessibilityLabel={`Aumentar ${s.rotulo}`}>
          <Text style={m.passoTxt}>+</Text>
        </Pressable>
      </View>
    </View>
  );
}

export default function MotorScreen() {
  const [vendedor, setVendedor] = useState(VEND[0]);
  const [params, setParams] = useState<Parametros>(PARAMETROS_PADRAO);
  const [paramsAbertos, setParamsAbertos] = useState(true);
  const [uf, setUf] = useState('RS');
  const [familia, setFamilia] = useState<string | null>(null);
  const [todosEventos, setTodosEventos] = useState(false);
  const [segmento, setSegmento] = useState('Todos');
  const [busca, setBusca] = useState('');
  const [escolhas, setEscolhas] = useState<Record<string, number[]>>({});
  const [abertos, setAbertos] = useState<Record<string, boolean>>({});
  const [troca, setTroca] = useState<{ i: number; slot: number } | null>(null);
  const [whats, setWhats] = useState<{ c: Cliente; itens: Recomendacao[] } | null>(null);

  const lista = VENDEDORES[vendedor];
  const chave = (i: number) => `${vendedor}#${i}`;

  // ranking de cada cliente + sugestões escolhidas
  const cards = useMemo(
    () =>
      lista.map((c, i) => {
        const rk = ranking(c, params);
        const ids = escolhas[`${vendedor}#${i}`] ?? rk.slice(0, SUGESTOES).map(r => r.produto.id);
        const rows = ids.map(id => rk.find(r => r.produto.id === id) ?? rk[0]);
        return { c, i, rk, ids, rows };
      }),
    [lista, params, escolhas, vendedor],
  );
  const total = cards.reduce((a, x) => a + x.rows.reduce((b, r) => b + preco(r.produto), 0), 0);
  const meta = params.metaPorCliente * lista.length;
  const pct = Math.min(100, Math.round((total / meta) * 100));
  const skusNovos = new Set(cards.flatMap(x => x.ids)).size;

  const segs = ['Todos', ...new Set(lista.map(c => c.segmento))];
  const seg = segs.includes(segmento) ? segmento : 'Todos';
  const q = normalizar(busca.trim());
  const visiveis = cards.filter(
    x => (seg === 'Todos' || x.c.segmento === seg) && normalizar(`${x.c.nome} ${x.c.cidade}`).includes(q),
  );

  // clima do estado escolhido
  const t = temperaturaMedia(uf);
  const u = umidadeMedia(uf);
  const cu = classeUmidade(u);
  const climaTxt =
    `${uf}: máxima média de ${t}°C e umidade de ${u}%. ` +
    (t >= params.limiteCalor ? 'Calor: bom momento para bebidas geladas.' : t <= 22 ? 'Mais frio: aposte em chás e energéticos.' : 'Temperatura amena.') +
    (cu ? ` Umidade ${cu}: ${dicaUmidade(cu)}` : '');

  // vendas
  const vendas: [string, number][] = (
    familia
      ? Object.entries(FAMILIAS.find(f => f.nome === familia)!.produtos)
      : FAMILIAS.map(f => [f.nome, f.volume] as [string, number])
  ).sort((a, b) => b[1] - a[1]);

  const evs = eventosFuturos();
  const mostrarEv = todosEventos ? evs : evs.slice(0, 3);

  const trocarVendedor = (v: string) => {
    setVendedor(v);
    setUf(VENDEDORES[v][0].uf);
  };

  const alternar = (i: number, c: Cliente) => {
    const k = chave(i);
    const abrir = !abertos[k];
    setAbertos({ ...abertos, [k]: abrir });
    if (abrir) setUf(c.uf);
  };

  const escolher = (id: number) => {
    if (!troca) return;
    const card = cards[troca.i];
    const ids = [...card.ids];
    ids[troca.slot] = id;
    setEscolhas({ ...escolhas, [chave(troca.i)]: ids });
    setTroca(null);
  };

  const fmtData = (d: Date) => d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });

  return (
    <View style={{ flex: 1, backgroundColor: cores.fundo }}>
      <View style={m.barra}>
        <Logo />
        <Text style={m.barraTit}>Motor de recomendação</Text>
      </View>

      <ScrollView contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 32 }} keyboardShouldPersistTaps="handled">
        {/* Vendedor */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6 }}>
          {VEND.map(v => (
            <Chip key={v} ativo={v === vendedor} onPress={() => trocarVendedor(v)} texto={v} />
          ))}
        </ScrollView>

        {/* Indicadores */}
        <View style={[m.linha, { marginTop: 12, gap: 6 }]}>
          <Text style={m.kpi}>{lista.length} clientes</Text>
          <Text style={m.kpi}>+R$ {Math.round(total / lista.length)}/cliente</Text>
          <Text style={m.kpi}>{skusNovos} SKUs novos</Text>
        </View>

        {/* Parâmetros */}
        <Card>
          <Pressable style={m.linha} onPress={() => setParamsAbertos(!paramsAbertos)} accessibilityRole="button">
            <View style={{ flex: 1 }}>
              <Titulo>Parâmetros do motor</Titulo>
            </View>
            <Text style={{ color: cores.mut, fontSize: 18 }}>{paramsAbertos ? '▴' : '▾'}</Text>
          </Pressable>
          <Sub>Ajuste quanto cada critério pesa. As sugestões e os gráficos mudam na hora.</Sub>
          {paramsAbertos && (
            <>
              {SLIDERS.map(s => (
                <Ajuste key={s.chave} s={s} valor={params[s.chave]} onChange={v => setParams({ ...params, [s.chave]: v })} />
              ))}
              <Pressable style={m.btnContorno} onPress={() => setParams(PARAMETROS_PADRAO)}>
                <Text style={{ color: cores.verde, fontWeight: '600' }}>Restaurar padrão</Text>
              </Pressable>
            </>
          )}
        </Card>

        {/* Clima */}
        <Card>
          <Titulo>Clima dos próximos 14 dias</Titulo>
          <Sub>{climaTxt}</Sub>
          <MapaBrasil uf={uf} onEstado={setUf} />
          <View style={[m.linha, { marginTop: 6 }]}>
            <Text style={m.pequeno}>mais frio</Text>
            <View style={m.gradiente}>
              {['#2A63B5', '#2A8FB5', '#2AB57A', '#B5932A', '#B54A2A'].map(c => (
                <View key={c} style={{ flex: 1, backgroundColor: c }} />
              ))}
            </View>
            <Text style={m.pequeno}>mais quente</Text>
          </View>
          <Text style={[m.forte, { marginTop: 16 }]}>Previsão em {uf}</Text>
          <ClimaGrafico dias={previsao(uf)} />
          <Text style={m.pequeno}>Laranja: temperatura máxima · Verde tracejado: umidade · Barras azuis: chance de chuva</Text>
        </Card>

        {/* Clientes */}
        <Card destaque>
          <Titulo>Meus clientes</Titulo>
          <Sub>Busque, filtre por tipo de estabelecimento e troque a sugestão de cada cliente para bater a meta.</Sub>
          <View style={m.caixa}>
            <Text style={{ fontSize: 13, color: cores.texto }}>
              <Text style={{ fontWeight: '700' }}>Meta da semana em itens novos: </Text>
              R$ {total} de R$ {meta} ({pct}%)
            </Text>
            <View style={m.trilhoMeta} accessibilityRole="progressbar" accessibilityValue={{ min: 0, max: 100, now: pct }}>
              <View style={[m.trilhoCheio, { width: `${pct}%` }]} />
            </View>
          </View>
          <TextInput
            value={busca}
            onChangeText={setBusca}
            placeholder="Buscar cliente ou cidade"
            placeholderTextColor={cores.mut}
            style={m.busca}
          />
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6, marginTop: 8 }}>
            {segs.map(s => (
              <Chip key={s} ativo={s === seg} onPress={() => setSegmento(s)} texto={s} />
            ))}
          </ScrollView>
          <Sub style={{ marginBottom: 6 }}>
            {visiveis.length} de {lista.length} clientes
          </Sub>
          {visiveis.length === 0 && <Text style={m.pequeno}>Nenhum cliente encontrado.</Text>}
          {visiveis.map(({ c, i, rk, ids, rows }) => {
            const aberto = !!abertos[chave(i)];
            const cl = classeUmidade(umidadeMedia(c.uf));
            return (
              <View key={c.nome} style={m.cliente}>
                <Pressable onPress={() => alternar(i, c)} style={m.linha} accessibilityRole="button" accessibilityState={{ expanded: aberto }}>
                  <View style={{ flex: 1 }}>
                    <View style={[m.linha, { flexWrap: 'wrap' }]}>
                      <Text style={m.forte}>{c.nome}</Text>
                      <Text style={m.tag}>{c.segmento}</Text>
                    </View>
                    <Text style={m.pequeno}>
                      {c.cidade} - {c.uf} · {c.mix.length} produtos no mix · {temperaturaMedia(c.uf)}°C · umidade {umidadeMedia(c.uf)}%
                    </Text>
                  </View>
                  <Text style={{ color: cores.mut, fontSize: 18 }}>{aberto ? '▴' : '▾'}</Text>
                </Pressable>
                {aberto && (
                  <View>
                    <Text style={m.oferecer}>Oferecer agora</Text>
                    {cl && (
                      <Text style={m.aviso}>
                        Umidade {cl} ({umidadeMedia(c.uf)}%): {dicaUmidade(cl)}
                      </Text>
                    )}
                    {rows.map((r, sIdx) => (
                      <View key={sIdx} style={m.sugestao}>
                        <View style={m.linha}>
                          <Pressable
                            style={m.select}
                            onPress={() => setTroca({ i, slot: sIdx })}
                            accessibilityLabel={`Sugestão ${sIdx + 1}: ${r.produto.nome}. Toque para trocar`}>
                            <Text style={{ fontWeight: '600', color: cores.texto, flex: 1 }}>{r.produto.nome}</Text>
                            <Text style={{ color: cores.mut }}>▾</Text>
                          </Pressable>
                          <Text style={m.score}>{r.pontos}</Text>
                        </View>
                        <Text style={m.pequeno}>
                          {r.motivos.join(' · ')} · R$ {preco(r.produto)}/cx
                          {ids[sIdx] !== rk[sIdx]?.produto.id && <Text style={m.ajustado}> · ajustado por você</Text>}
                        </Text>
                      </View>
                    ))}
                    <Text style={[m.forte, { fontSize: 13, marginTop: 10, marginBottom: 6 }]}>Por que essas sugestões?</Text>
                    <Pontuacao itens={rows} />
                    <Pressable style={m.btnVerde} onPress={() => setWhats({ c, itens: rows })}>
                      <Icone d={ICONES.chat} tamanho={18} />
                      <Text style={{ color: '#fff', fontWeight: '600' }}>Enviar pelo Pede Fruki (WhatsApp)</Text>
                    </Pressable>
                  </View>
                )}
              </View>
            );
          })}
        </Card>

        {/* Vendas */}
        <Card>
          <Titulo>{familia ? `Produtos: ${familia}` : 'Vendas por família de produtos'}</Titulo>
          <Sub>
            Do mais para o menos vendido, em % do total de 12 meses.
            {familia ? '' : ' Toque em uma coluna para ver os produtos.'}
          </Sub>
          {familia && (
            <Pressable style={[m.btnContorno, { alignSelf: 'flex-start', marginTop: 0 }]} onPress={() => setFamilia(null)}>
              <Text style={{ color: cores.verde, fontWeight: '600' }}>← Voltar às famílias</Text>
            </Pressable>
          )}
          <Barras itens={vendas} onBarra={familia ? undefined : setFamilia} />
          <View style={[m.linha, { gap: 8, marginTop: 10, alignItems: 'stretch' }]}>
            <Destaque cor={cores.verde} rotulo="Mais vendido" item={vendas[0]} />
            <Destaque cor={cores.laranja} rotulo="Menos vendido" item={vendas[vendas.length - 1]} />
          </View>
        </Card>

        {/* Eventos */}
        <Card>
          <Titulo>Eventos e datas comemorativas</Titulo>
          <Sub>Em laranja: faltam até 14 dias.</Sub>
          {mostrarEv.map(e => {
            const d = dias(e);
            const breve = d <= 14;
            return (
              <View key={e.nome} style={[m.evento, breve && m.eventoBreve]}>
                <View style={{ flex: 1 }}>
                  <Text style={{ fontSize: 15, color: cores.texto }}>
                    {e.nome}  <Text style={{ fontSize: 12, color: cores.mut }}>{e.abrangencia}</Text>
                  </Text>
                  <Text style={m.pequeno}>
                    {fmtData(e.data)}
                    {e.familias.length ? ` · puxa ${e.familias.join(', ').toLowerCase()}` : ''}
                  </Text>
                </View>
                <Text style={{ fontWeight: '700', color: breve ? cores.laranja : cores.texto }}>
                  {d === 0 ? 'hoje' : `em ${d} d`}
                </Text>
              </View>
            );
          })}
          {evs.length > 3 && (
            <Pressable style={m.btnTonal} onPress={() => setTodosEventos(!todosEventos)}>
              <Text style={{ color: cores.verdeEscuro, fontWeight: '600' }}>
                {todosEventos ? 'Ver menos' : `Ver mais (${evs.length - 3})`}
              </Text>
            </Pressable>
          )}
        </Card>

        <Text style={[m.pequeno, { marginTop: 14 }]}>
          Protótipo com dados simulados: clima, vendas e clientes. A pontuação combina afinidade com clientes do mesmo
          segmento, previsão de temperatura e umidade e o próximo evento. O contorno dos estados é simplificado.
        </Text>
      </ScrollView>

      {/* Troca de sugestão */}
      <Modal visible={!!troca} transparent animationType="fade" onRequestClose={() => setTroca(null)}>
        <Pressable style={m.veu} onPress={() => setTroca(null)}>
          <View style={m.folha}>
            <Text style={[m.forte, { marginBottom: 8 }]}>Trocar sugestão</Text>
            {troca &&
              cards[troca.i].rk
                .filter(x => x.produto.id === cards[troca.i].ids[troca.slot] || !cards[troca.i].ids.includes(x.produto.id))
                .map(x => {
                  const atual = x.produto.id === cards[troca.i].ids[troca.slot];
                  return (
                    <Pressable key={x.produto.id} style={[m.opcao, atual && { backgroundColor: cores.verdeClaro }]} onPress={() => escolher(x.produto.id)}>
                      <Text style={{ flex: 1, color: cores.texto, fontWeight: atual ? '700' : '400' }}>{x.produto.nome}</Text>
                      <Text style={m.score}>{x.pontos}</Text>
                    </Pressable>
                  );
                })}
          </View>
        </Pressable>
      </Modal>

      {/* Prévia WhatsApp */}
      <Modal visible={!!whats} transparent animationType="slide" onRequestClose={() => setWhats(null)}>
        <View style={m.veu}>
          <View style={[m.folha, { backgroundColor: cores.whatsFundo }]}>
            <Text style={[m.forte, { marginBottom: 10 }]}>Prévia no WhatsApp · Pede Fruki</Text>
            {whats && (
              <View style={m.balao}>
                <Text style={{ color: cores.texto, fontSize: 14, lineHeight: 20 }}>
                  {`Olá, ${whats.c.nome}! 👋\nAqui é o Pede Fruki. Separamos sugestões para a sua loja nesta semana:\n\n` +
                    whats.itens
                      .map((r, i) => `${i + 1}. ${r.produto.nome} — R$ ${preco(r.produto)}/cx` + (r.motivos[1] ? `\n   ${r.motivos[1]}` : ''))
                      .join('\n') +
                    '\n\nResponda com o número do item para incluir no seu pedido. 🥤'}
                </Text>
              </View>
            )}
            <Pressable style={[m.btnVerde, { marginTop: 14 }]} onPress={() => setWhats(null)}>
              <Text style={{ color: '#fff', fontWeight: '600' }}>Fechar</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </View>
  );
}

function Chip({ ativo, texto, onPress }: { ativo: boolean; texto: string; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[m.chip, ativo && m.chipAtivo]} accessibilityState={{ selected: ativo }}>
      <Text style={{ color: ativo ? '#fff' : cores.texto, fontWeight: ativo ? '700' : '400', fontSize: 13 }}>{texto}</Text>
    </Pressable>
  );
}

function Destaque({ cor, rotulo, item }: { cor: string; rotulo: string; item: [string, number] }) {
  return (
    <View style={[m.caixa, { flex: 1, borderLeftWidth: 4, borderLeftColor: cor }]}>
      <Text style={m.pequeno}>{rotulo}</Text>
      <Text style={m.forte}>{item[0]}</Text>
      <Text style={m.pequeno}>{item[1]} mil un.</Text>
    </View>
  );
}

const m = StyleSheet.create({
  barra: { height: 56, flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16 },
  barraTit: { flex: 1, fontSize: 19, fontWeight: '800', color: cores.verdeEscuro, marginLeft: 8 },
  linha: { flexDirection: 'row', alignItems: 'center' },
  kpi: {
    flex: 1,
    textAlign: 'center',
    paddingVertical: 10,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: cores.borda,
    color: cores.verdeMedio,
    fontSize: 12,
    backgroundColor: '#fff',
  },
  ajuste: { paddingVertical: 6 },
  passo: {
    width: 36,
    height: 36,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: cores.borda,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  passoTxt: { fontSize: 20, color: cores.verdeEscuro, fontWeight: '700', marginTop: -2 },
  trilho: { flex: 1, height: 6, borderRadius: 4, backgroundColor: cores.trilho, marginHorizontal: 10, overflow: 'hidden' },
  trilhoMeta: { height: 8, borderRadius: 6, backgroundColor: cores.trilho, marginTop: 8, overflow: 'hidden' },
  trilhoCheio: { height: '100%', backgroundColor: cores.verde, borderRadius: 6 },
  btnContorno: {
    marginTop: 10,
    borderWidth: 1,
    borderColor: cores.verde,
    borderRadius: 20,
    paddingVertical: 10,
    paddingHorizontal: 16,
    alignItems: 'center',
  },
  btnTonal: { marginTop: 8, backgroundColor: cores.verdeClaro, borderRadius: 20, paddingVertical: 12, alignItems: 'center' },
  btnVerde: {
    marginTop: 8,
    backgroundColor: cores.verde,
    borderRadius: 20,
    paddingVertical: 12,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 8,
  },
  pequeno: { fontSize: 12, color: cores.mut },
  forte: { fontSize: 15, fontWeight: '700', color: cores.texto },
  gradiente: { flex: 1, height: 8, borderRadius: 6, overflow: 'hidden', flexDirection: 'row', marginHorizontal: 8 },
  caixa: { backgroundColor: '#fff', borderWidth: 1, borderColor: cores.borda, borderRadius: 14, padding: 12 },
  busca: {
    marginTop: 10,
    height: 48,
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: 14,
    paddingHorizontal: 14,
    backgroundColor: '#fff',
    color: cores.texto,
  },
  chip: { borderWidth: 1, borderColor: cores.borda, borderRadius: 20, paddingHorizontal: 14, paddingVertical: 7, backgroundColor: '#fff' },
  chipAtivo: { backgroundColor: cores.verde, borderColor: cores.verde },
  cliente: { backgroundColor: '#fff', borderWidth: 1, borderColor: cores.borda, borderRadius: 14, padding: 12, marginBottom: 8 },
  tag: {
    fontSize: 11,
    color: cores.verdeMedio,
    backgroundColor: cores.verdeClaro,
    borderRadius: 20,
    paddingHorizontal: 8,
    paddingVertical: 1,
    marginLeft: 6,
    overflow: 'hidden',
  },
  oferecer: { fontSize: 13, fontWeight: '800', color: cores.verde, marginTop: 10, marginBottom: 4 },
  aviso: { fontSize: 12, color: cores.laranja, fontWeight: '600', marginBottom: 4 },
  sugestao: { borderTopWidth: 1, borderTopColor: cores.borda, paddingVertical: 8 },
  select: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    marginRight: 8,
  },
  score: {
    backgroundColor: cores.verde,
    color: '#fff',
    fontWeight: '800',
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 2,
    overflow: 'hidden',
  },
  ajustado: { color: cores.laranja, fontWeight: '700' },
  evento: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: cores.borda },
  eventoBreve: { borderLeftWidth: 4, borderLeftColor: cores.laranja, paddingLeft: 10, backgroundColor: '#FFF6EF', borderRadius: 8 },
  veu: { flex: 1, backgroundColor: 'rgba(0,0,0,0.35)', justifyContent: 'flex-end' },
  folha: { backgroundColor: '#fff', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 18, paddingBottom: 28 },
  opcao: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, paddingHorizontal: 10, borderRadius: 10 },
  balao: {
    backgroundColor: cores.whatsBalao,
    borderRadius: 12,
    borderTopLeftRadius: 2,
    padding: 12,
    marginRight: 40,
  },
});
