// Mapa: Brasil por região, com tour animado que começa pelo Sul (onde estão os principais clientes).
import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Animated, Easing, Pressable, Text, View } from 'react-native';

import { Card, ICONES, Icone, Sub, Titulo } from '../../components/Base';
import { ClimaGrafico } from '../../components/Graficos';
import { MapaRegioes } from '../../components/MapaRegioes';
import { NOME_UF, REGIOES_TOUR, RegiaoId } from '../../data/dados';
import {
  classeUmidade,
  dicaUmidade,
  previsao,
  temperaturaMedia,
  umidadeMedia,
} from '../../motor/clima';
import { dias, eventosFuturos } from '../../motor/motor';
import { cores } from '../../theme';
import { Motor, Nav } from '../estado';
import { Chip, Progresso, Selo, realce, u } from '../ui';

/** Tempo em cada região durante o tour (a animação de zoom leva ~1,1 s). */
const TEMPO_REGIAO = 5000;

const regiaoDaUf = (uf: string) =>
  REGIOES_TOUR.find(r => r.ufs.includes(uf))!.id;
const media = (xs: number[]) =>
  Math.round(xs.reduce((a, x) => a + x, 0) / Math.max(1, xs.length));

export default function Mapa({ m, nav }: { m: Motor; nav: Nav }) {
  const [regiao, setRegiao] = useState<RegiaoId | null>('S');
  const [uf, setUf] = useState('RS');
  const [tocando, setTocando] = useState(false);
  const progresso = useRef(new Animated.Value(0)).current;

  const porUf = useMemo(() => {
    const n: Record<string, number> = {};
    m.todos.forEach(c => (n[c.uf] = (n[c.uf] ?? 0) + 1));
    return n;
  }, [m.todos]);

  const info = regiao ? REGIOES_TOUR.find(r => r.id === regiao)! : null;
  const ufs = info ? info.ufs : REGIOES_TOUR.flatMap(r => r.ufs);
  const clientes = m.todos.filter(c => ufs.includes(c.uf));

  /** Vai para a região e escolhe o estado com mais clientes (ou o primeiro). */
  const focar = (r: RegiaoId | null) => {
    setRegiao(r);
    const lista = r ? REGIOES_TOUR.find(x => x.id === r)!.ufs : ['RS'];
    setUf([...lista].sort((a, b) => (porUf[b] ?? 0) - (porUf[a] ?? 0))[0]);
  };

  // Tour: Sul → Sudeste → Centro-Oeste → Nordeste → Norte → Brasil inteiro.
  useEffect(() => {
    if (!tocando) return;
    progresso.setValue(0);
    const anim = Animated.timing(progresso, {
      toValue: 1,
      duration: TEMPO_REGIAO,
      easing: Easing.linear,
      useNativeDriver: false,
    });
    anim.start();
    const timer = setTimeout(() => {
      const i = REGIOES_TOUR.findIndex(r => r.id === regiao);
      if (i < 0 || i === REGIOES_TOUR.length - 1) {
        focar(null);
        setTocando(false);
      } else {
        focar(REGIOES_TOUR[i + 1].id);
      }
    }, TEMPO_REGIAO);
    return () => {
      clearTimeout(timer);
      anim.stop();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tocando, regiao]);

  const iniciarTour = () => {
    if (tocando) return setTocando(false);
    focar('S');
    setTocando(true);
  };

  const pular = (passo: number) => {
    const i = REGIOES_TOUR.findIndex(r => r.id === regiao);
    const prox =
      i < 0
        ? passo > 0
          ? 0
          : REGIOES_TOUR.length - 1
        : (i + passo + REGIOES_TOUR.length) % REGIOES_TOUR.length;
    focar(REGIOES_TOUR[prox].id);
  };

  const tocarEstado = (x: string) => {
    setTocando(false);
    if (regiao && regiaoDaUf(x) !== regiao) setRegiao(regiaoDaUf(x));
    setUf(x);
  };

  const tMedia = media(ufs.map(temperaturaMedia));
  const uMedia = media(ufs.map(umidadeMedia));
  const cu = classeUmidade(uMedia);
  const vendedores = new Set(clientes.map(c => c.vendedor)).size;
  const quente = [...ufs].sort(
    (a, b) => temperaturaMedia(b) - temperaturaMedia(a),
  )[0];
  const evento = eventosFuturos().find(
    e =>
      e.abrangencia === 'Nacional' ||
      (info && e.abrangencia.includes(info.nome)),
  );

  const mapa = (
    <Card style={{ marginTop: 0 }}>
      <View style={[u.linha, { flexWrap: 'wrap', gap: 8 }]}>
        <View style={{ flex: 1, minWidth: 200 }}>
          <Titulo>{info ? `Região ${info.nome}` : 'Brasil'}</Titulo>
          <Text style={u.pequeno}>
            {tocando
              ? 'Tour pelas regiões, começando pelo Sul'
              : 'Toque em um estado para ver a previsão'}
          </Text>
        </View>
        <View style={[u.linha, { gap: 8 }]}>
          <Pressable
            onPress={() => pular(-1)}
            accessibilityLabel="Região anterior"
            style={realce(botaoRedondo)}
          >
            <Icone d={ICONES.esquerda} cor={cores.verdeEscuro} tamanho={22} />
          </Pressable>
          <Pressable
            onPress={iniciarTour}
            accessibilityRole="button"
            style={realce(
              [
                u.linha,
                {
                  gap: 6,
                  backgroundColor: cores.verde,
                  borderRadius: 22,
                  paddingHorizontal: 16,
                  paddingVertical: 10,
                },
              ],
              {
                backgroundColor: cores.verdeMedio,
              },
            )}
          >
            <Icone
              d={tocando ? ICONES.pausa : ICONES.play}
              cor="#fff"
              tamanho={18}
            />
            <Text style={{ color: '#fff', fontWeight: '700' }}>
              {tocando ? 'Pausar tour' : 'Tour pelas regiões'}
            </Text>
          </Pressable>
          <Pressable
            onPress={() => pular(1)}
            accessibilityLabel="Próxima região"
            style={realce(botaoRedondo)}
          >
            <Icone d={ICONES.direita} cor={cores.verdeEscuro} tamanho={22} />
          </Pressable>
        </View>
      </View>

      <View
        style={{
          flexDirection: 'row',
          flexWrap: 'wrap',
          gap: 6,
          marginTop: 12,
        }}
      >
        {REGIOES_TOUR.map((r, i) => (
          <Chip
            key={r.id}
            ativo={regiao === r.id}
            texto={`${i + 1}. ${r.nome}`}
            onPress={() => {
              setTocando(false);
              focar(r.id);
            }}
          />
        ))}
        <Chip
          ativo={regiao === null}
          texto="Brasil inteiro"
          onPress={() => {
            setTocando(false);
            focar(null);
          }}
        />
      </View>
      {tocando && (
        <View
          style={{
            height: 4,
            backgroundColor: cores.trilho,
            borderRadius: 2,
            marginTop: 10,
            overflow: 'hidden',
          }}
        >
          <Animated.View
            style={{
              height: '100%',
              backgroundColor: cores.verde,
              width: progresso.interpolate({
                inputRange: [0, 1],
                outputRange: ['0%', '100%'],
              }),
            }}
          />
        </View>
      )}

      <View style={{ marginTop: 12 }}>
        <MapaRegioes
          regiao={regiao}
          uf={uf}
          onEstado={tocarEstado}
          clientesPorUf={porUf}
          altura={nav.largo ? 460 : 340}
        />
      </View>
      <View style={[u.linha, { marginTop: 8, gap: 8, flexWrap: 'wrap' }]}>
        <Text style={u.pequeno}>mais frio</Text>
        <View
          style={{
            flex: 1,
            minWidth: 120,
            height: 8,
            borderRadius: 6,
            overflow: 'hidden',
            flexDirection: 'row',
          }}
        >
          {['#2A63B5', '#2A8FB5', '#2AB57A', '#B5932A', '#B54A2A'].map(c => (
            <View key={c} style={{ flex: 1, backgroundColor: c }} />
          ))}
        </View>
        <Text style={u.pequeno}>mais quente</Text>
        <View style={[u.linha, { gap: 4, marginLeft: 8 }]}>
          <View
            style={{
              width: 14,
              height: 14,
              borderRadius: 7,
              backgroundColor: cores.verdeEscuro,
            }}
          />
          <Text style={u.pequeno}>nº de clientes</Text>
        </View>
      </View>
    </Card>
  );

  const painel = (
    <View>
      <Card style={{ marginTop: nav.largo ? 0 : 16 }}>
        <Text
          style={{
            fontSize: 13,
            fontWeight: '800',
            color: cores.verde,
            letterSpacing: 0.5,
          }}
        >
          {info ? info.nome.toUpperCase() : 'BRASIL'}
        </Text>
        <Text style={[u.pequeno, { marginTop: 2 }]}>{info ? ufs.join(' · ') : '27 estados, 5 regiões'}</Text>
        <View
          style={{
            flexDirection: 'row',
            flexWrap: 'wrap',
            gap: 8,
            marginTop: 12,
          }}
        >
          <Numero rotulo="clientes" valor={String(clientes.length)} />
          <Numero
            rotulo={vendedores === 1 ? 'vendedor' : 'vendedores'}
            valor={String(vendedores)}
          />
          <Numero
            rotulo="máx. média"
            valor={`${tMedia}°C`}
            cor={cores.laranja}
          />
          <Numero rotulo="umidade" valor={`${uMedia}%`} cor={cores.azul} />
        </View>
        <View
          style={{
            backgroundColor: cores.verdeClaro,
            borderRadius: 12,
            padding: 12,
            marginTop: 12,
          }}
        >
          <Text
            style={{
              fontWeight: '700',
              color: cores.verdeEscuro,
              marginBottom: 2,
            }}
          >
            Para as visitas
          </Text>
          <Text style={{ fontSize: 13, color: cores.texto }}>
            {tMedia >= m.params.limiteCalor
              ? `Calor (até ${temperaturaMedia(quente)}°C em ${
                  NOME_UF[quente]
                }): bebidas geladas, PET e lata.`
              : tMedia <= 22
              ? 'Mais frio: chás e energéticos vendem melhor.'
              : `Temperatura amena; o mais quente é ${
                  NOME_UF[quente]
                } (${temperaturaMedia(quente)}°C).`}
            {cu ? ` Umidade ${cu}: ${dicaUmidade(cu)}` : ''}
            {evento
              ? ` Próxima data: ${evento.nome} em ${dias(evento)} dias.`
              : ''}
          </Text>
        </View>
        {clientes.length === 0 && (
          <Text style={[u.pequeno, { marginTop: 10 }]}>
            Sem clientes na carteira: região de expansão.
          </Text>
        )}
      </Card>

      <Card>
        <Titulo>Estados</Titulo>
        <Sub>Toque para ver a previsão de 14 dias.</Sub>
        {[...ufs]
          .sort(
            (a, b) =>
              (porUf[b] ?? 0) - (porUf[a] ?? 0) ||
              temperaturaMedia(b) - temperaturaMedia(a),
          )
          .slice(0, info ? 9 : 8)
          .map(x => (
            <Pressable
              key={x}
              onPress={() => tocarEstado(x)}
              style={realce([
                u.linha,
                {
                  gap: 10,
                  paddingVertical: 8,
                  paddingHorizontal: 8,
                  borderRadius: 10,
                },
                x === uf && { backgroundColor: cores.verdeClaro },
              ])}
            >
              <Text
                style={{
                  width: 30,
                  fontWeight: '800',
                  color: cores.verdeEscuro,
                }}
              >
                {x}
              </Text>
              <View style={{ flex: 1 }}>
                <Text style={{ fontSize: 13, color: cores.texto }}>
                  {NOME_UF[x]}
                </Text>
                <Progresso
                  pct={
                    ((porUf[x] ?? 0) /
                      Math.max(1, ...ufs.map(y => porUf[y] ?? 0))) *
                    100
                  }
                  altura={4}
                />
              </View>
              <Text style={[u.pequeno, { width: 74, textAlign: 'right' }]}>
                {porUf[x] ?? 0} clientes
              </Text>
              <Text
                style={{
                  width: 44,
                  textAlign: 'right',
                  color: cores.laranja,
                  fontWeight: '700',
                }}
              >
                {temperaturaMedia(x)}°
              </Text>
            </Pressable>
          ))}
      </Card>

      {clientes.length > 0 && (
        <Card>
          <Titulo>Clientes {info ? `no ${info.nome}` : 'no Brasil'}</Titulo>
          <Sub>Toque para abrir as sugestões.</Sub>
          {clientes.slice(0, 6).map(c => (
            <Pressable
              key={`${c.vendedor}#${c.id ?? c.nome}`}
              onPress={() => nav.abrirCliente(c.vendedor, c)}
              style={realce([
                u.linha,
                {
                  gap: 10,
                  paddingVertical: 8,
                  paddingHorizontal: 8,
                  borderRadius: 10,
                },
              ])}
            >
              <Icone d={ICONES.local} cor={cores.verde} tamanho={18} />
              <View style={{ flex: 1 }}>
                <Text
                  style={{
                    fontSize: 14,
                    fontWeight: '600',
                    color: cores.texto,
                  }}
                >
                  {c.nome}
                </Text>
                <Text style={u.pequeno}>
                  {c.cidade} - {c.uf} · {c.vendedor}
                </Text>
              </View>
              <Selo texto={c.segmento} cor={cores.mut} fundo={cores.app} />
            </Pressable>
          ))}
          {clientes.length > 6 && (
            <Text style={[u.pequeno, { marginTop: 4 }]}>
              e mais {clientes.length - 6} clientes
            </Text>
          )}
        </Card>
      )}
    </View>
  );

  return (
    <View>
      {nav.largo ? (
        <View
          style={{ flexDirection: 'row', gap: 16, alignItems: 'flex-start' }}
        >
          <View style={{ flex: 3, minWidth: 0 }}>{mapa}</View>
          <View style={{ flex: 2, minWidth: 0 }}>{painel}</View>
        </View>
      ) : (
        <>
          {mapa}
          {painel}
        </>
      )}
      <Card>
        <View style={[u.linha, { gap: 8 }]}>
          <View style={{ flex: 1 }}>
            <Titulo>Previsão em {NOME_UF[uf]}</Titulo>
            <Text style={u.pequeno}>
              Máxima média de {temperaturaMedia(uf)}°C e umidade de{' '}
              {umidadeMedia(uf)}% nos próximos 14 dias
            </Text>
          </View>
          <Selo texto={`${porUf[uf] ?? 0} ${porUf[uf] === 1 ? 'cliente' : 'clientes'}`} />
        </View>
        <View style={{ marginTop: 10 }}>
          <ClimaGrafico dias={previsao(uf)} />
        </View>
        <Text style={u.pequeno}>
          Laranja: temperatura máxima · Verde tracejado: umidade · Barras azuis:
          chance de chuva
        </Text>
      </Card>
    </View>
  );
}

const botaoRedondo = {
  width: 40,
  height: 40,
  borderRadius: 20,
  borderWidth: 1,
  borderColor: cores.borda,
  alignItems: 'center' as const,
  justifyContent: 'center' as const,
  backgroundColor: '#fff',
};

function Numero({
  rotulo,
  valor,
  cor = cores.verdeEscuro,
}: {
  rotulo: string;
  valor: string;
  cor?: string;
}) {
  return (
    <View
      style={{
        flexGrow: 1,
        flexBasis: 90,
        backgroundColor: cores.app,
        borderRadius: 12,
        padding: 10,
      }}
    >
      <Text style={{ fontSize: 20, fontWeight: '800', color: cor }}>
        {valor}
      </Text>
      <Text style={u.pequeno}>{rotulo}</Text>
    </View>
  );
}
