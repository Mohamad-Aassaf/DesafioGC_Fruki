package com.fruki.vendedor;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fruki.vendedor.data.Dados;
import com.fruki.vendedor.data.Modelos.Cliente;
import com.fruki.vendedor.data.Modelos.Evento;
import com.fruki.vendedor.data.Modelos.Familia;
import com.fruki.vendedor.motor.Clima;
import com.fruki.vendedor.motor.MotorRecomendacao;
import com.fruki.vendedor.motor.MotorRecomendacao.Recomendacao;
import com.fruki.vendedor.motor.Parametros;
import com.fruki.vendedor.ui.BarrasView;
import com.fruki.vendedor.ui.ClimaGraficoView;
import com.fruki.vendedor.ui.MapaBrasilView;
import com.fruki.vendedor.ui.PontuacaoView;
import com.fruki.vendedor.ui.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Motor de recomendação: parâmetros, clima, clientes com sugestões, vendas e eventos. */
public class RecomendacaoActivity extends AppCompatActivity {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final int SUGESTOES = 3;

    private final Parametros params = new Parametros();
    private String uf = "RS";
    private String familiaAberta;
    private boolean todosEventos;
    private boolean parametrosAbertos = true;
    private String filtroSegmento = "Todos";
    private String busca = "";
    /** Sugestões trocadas pelo vendedor: chave "vendedor#índice" -> ids escolhidos. */
    private final Map<String, int[]> escolhas = new HashMap<>();
    private final Set<String> abertos = new HashSet<>();

    private Spinner spVendedor;
    private TextView txtClima, tituloGraficoClima, txtMeta, txtContagem;
    private TextView tituloVendas, subVendas, txtMaisVendido, txtMenosVendido;
    private TextView kpiClientes, kpiTicket, kpiNovos, setaParametros;
    private MapaBrasilView mapa;
    private ClimaGraficoView graficoClima;
    private BarrasView barras;
    private MaterialButton btnVoltarFamilias, btnEventos;
    private ProgressBar progMeta;
    private ChipGroup chips;
    private LinearLayout listaClientes, listaEventos, listaParametros;
    private final List<Runnable> atualizarSliders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendacao);
        Ui.bordaABorda(this, findViewById(R.id.raiz));

        spVendedor = findViewById(R.id.spVendedor);
        txtClima = findViewById(R.id.txtClima);
        tituloGraficoClima = findViewById(R.id.tituloGraficoClima);
        txtMeta = findViewById(R.id.txtMeta);
        txtContagem = findViewById(R.id.txtContagem);
        tituloVendas = findViewById(R.id.tituloVendas);
        subVendas = findViewById(R.id.subVendas);
        txtMaisVendido = findViewById(R.id.txtMaisVendido);
        txtMenosVendido = findViewById(R.id.txtMenosVendido);
        kpiClientes = findViewById(R.id.kpiClientes);
        kpiTicket = findViewById(R.id.kpiTicket);
        kpiNovos = findViewById(R.id.kpiNovos);
        setaParametros = findViewById(R.id.setaParametros);
        mapa = findViewById(R.id.mapa);
        graficoClima = findViewById(R.id.graficoClima);
        barras = findViewById(R.id.barras);
        btnVoltarFamilias = findViewById(R.id.btnVoltarFamilias);
        btnEventos = findViewById(R.id.btnEventos);
        progMeta = findViewById(R.id.progMeta);
        chips = findViewById(R.id.chips);
        listaClientes = findViewById(R.id.listaClientes);
        listaEventos = findViewById(R.id.listaEventos);
        listaParametros = findViewById(R.id.listaParametros);

        ArrayAdapter<String> vend = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new ArrayList<>(Dados.VENDEDORES.keySet()));
        vend.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVendedor.setAdapter(vend);
        spVendedor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                List<Cliente> l = clientes();
                if (!l.isEmpty()) irPara(l.get(0).uf);
                desenharClientes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        mapa.setOnEstadoListener(this::irPara);
        barras.setOnBarraListener(nome -> { familiaAberta = nome; desenharVendas(); });
        btnVoltarFamilias.setOnClickListener(v -> { familiaAberta = null; desenharVendas(); });
        btnEventos.setOnClickListener(v -> { todosEventos = !todosEventos; desenharEventos(); });

        EditText ed = findViewById(R.id.edBusca);
        ed.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { busca = s.toString(); desenharClientes(); }
        });

        findViewById(R.id.cabParametros).setOnClickListener(v -> {
            parametrosAbertos = !parametrosAbertos;
            listaParametros.setVisibility(parametrosAbertos ? View.VISIBLE : View.GONE);
            setaParametros.setText(parametrosAbertos ? "▴" : "▾");
        });
        setaParametros.setText("▴");
        montarParametros();

        irPara(uf);
        desenharVendas();
        desenharEventos();
        desenharClientes();
    }

    // ---------- Parâmetros ----------

    private void montarParametros() {
        slider("Peso do histórico e segmento", 0, 200, "%", () -> params.pesoHistorico, v -> params.pesoHistorico = v);
        slider("Bônus de calor", 0, 30, " pts", () -> params.bonusCalor, v -> params.bonusCalor = v);
        slider("Calor a partir de", 20, 34, "°C", () -> params.limiteCalor, v -> params.limiteCalor = v);
        slider("Bônus de hidratação (umidade)", 0, 20, " pts", () -> params.bonusUmidade, v -> params.bonusUmidade = v);
        slider("Bônus de evento próximo", 0, 25, " pts", () -> params.bonusEvento, v -> params.bonusEvento = v);
        slider("Olhar eventos dos próximos", 7, 120, " dias", () -> params.janelaEventos, v -> params.janelaEventos = v);
        slider("Meta semanal por cliente", 50, 300, "", () -> params.metaPorCliente, v -> params.metaPorCliente = v);

        MaterialButton restaurar = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        restaurar.setText("Restaurar padrão");
        restaurar.setAllCaps(false);
        restaurar.setOnClickListener(v -> {
            params.restaurar();
            for (Runnable r : atualizarSliders) r.run();
            desenharClientes();
        });
        listaParametros.addView(restaurar);
    }

    private void slider(String rotulo, int min, int max, String sufixo, IntSupplier get, IntConsumer set) {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.VERTICAL);
        linha.setPadding(0, Ui.dpi(this, 4), 0, Ui.dpi(this, 4));

        LinearLayout topo = new LinearLayout(this);
        TextView nome = texto(rotulo, 13, R.color.texto, false);
        topo.addView(nome, new LinearLayout.LayoutParams(0, -2, 1));
        TextView valor = texto("", 13, R.color.fruki_verde_medio, true);
        topo.addView(valor);
        linha.addView(topo);

        SeekBar sb = new SeekBar(this);
        sb.setMin(min);
        sb.setMax(max);
        sb.setContentDescription(rotulo);
        linha.addView(sb);
        Runnable sync = () -> {
            int v = get.getAsInt();
            sb.setProgress(v);
            valor.setText(formatarParametro(v, sufixo));
        };
        sync.run();
        atualizarSliders.add(sync);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int v, boolean user) {
                if (!user) return;
                set.accept(v);
                valor.setText(formatarParametro(v, sufixo));
                desenharClientes();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        listaParametros.addView(linha);
    }

    private static String formatarParametro(int v, String sufixo) {
        return sufixo.isEmpty() ? "R$ " + v : v + sufixo;
    }

    // ---------- Clima ----------

    private void irPara(String novaUf) {
        uf = novaUf;
        mapa.setSelecionado(uf);
        int t = Clima.temperaturaMedia(uf), u = Clima.umidadeMedia(uf);
        String classe = Clima.classeUmidade(u);
        String txt = uf + ": máxima média de " + t + "°C e umidade de " + u + "%. "
                + (t >= params.limiteCalor ? "Calor: bom momento para bebidas geladas."
                : t <= 22 ? "Mais frio: aposte em chás e energéticos." : "Temperatura amena.")
                + (classe != null ? " Umidade " + classe + ": " + Clima.dicaUmidade(classe) : "");
        txtClima.setText(txt);
        tituloGraficoClima.setText("Previsão em " + uf);
        graficoClima.setDias(Clima.previsao(uf));
    }

    // ---------- Vendas ----------

    private void desenharVendas() {
        List<Map.Entry<String, Integer>> e = new ArrayList<>();
        if (familiaAberta == null) {
            for (Familia f : Dados.FAMILIAS.values()) e.add(new java.util.AbstractMap.SimpleEntry<>(f.nome, f.volume));
        } else {
            e.addAll(Dados.FAMILIAS.get(familiaAberta).produtos.entrySet());
        }
        e.sort((a, b) -> b.getValue() - a.getValue());
        List<String> nomes = new ArrayList<>();
        List<Integer> vals = new ArrayList<>();
        for (Map.Entry<String, Integer> x : e) { nomes.add(x.getKey()); vals.add(x.getValue()); }
        barras.setDados(nomes, vals, familiaAberta == null);

        tituloVendas.setText(familiaAberta == null ? "Vendas por família de produtos" : "Produtos: " + familiaAberta);
        subVendas.setText("Do mais para o menos vendido, em % do total de 12 meses."
                + (familiaAberta == null ? " Toque em uma coluna para ver os produtos." : ""));
        btnVoltarFamilias.setVisibility(familiaAberta == null ? View.GONE : View.VISIBLE);
        Map.Entry<String, Integer> mais = e.get(0), menos = e.get(e.size() - 1);
        txtMaisVendido.setText(destaque("Mais vendido", mais.getKey(), mais.getValue() + " mil un.", R.color.fruki_verde));
        txtMenosVendido.setText(destaque("Menos vendido", menos.getKey(), menos.getValue() + " mil un.", R.color.fruki_laranja));
    }

    private CharSequence destaque(String rotulo, String nome, String valor, int cor) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int i = sb.length();
        sb.append("● ").append(rotulo).append('\n');
        sb.setSpan(new ForegroundColorSpan(Ui.cor(this, cor)), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int j = sb.length();
        sb.append(nome).append('\n');
        sb.setSpan(new StyleSpan(Typeface.BOLD), j, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(Ui.cor(this, R.color.texto)), j, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new RelativeSizeSpan(1.12f), j, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(valor);
        return sb;
    }

    // ---------- Eventos ----------

    private void desenharEventos() {
        listaEventos.removeAllViews();
        List<Evento> l = MotorRecomendacao.eventosFuturos();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", PT_BR);
        int n = todosEventos ? l.size() : Math.min(3, l.size());
        for (int i = 0; i < n; i++) {
            Evento e = l.get(i);
            long d = MotorRecomendacao.dias(e);
            boolean breve = d <= 14;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Ui.dpi(this, breve ? 12 : 0), Ui.dpi(this, 10), Ui.dpi(this, 8), Ui.dpi(this, 10));
            if (breve) row.setBackgroundResource(R.drawable.bg_inner_alerta);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = Ui.dpi(this, 4);
            LinearLayout txt = new LinearLayout(this);
            txt.setOrientation(LinearLayout.VERTICAL);
            SpannableStringBuilder sb = new SpannableStringBuilder(e.nome + "  ");
            int k = sb.length();
            sb.append(e.abrangencia);
            sb.setSpan(new RelativeSizeSpan(.8f), k, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Ui.cor(this, R.color.texto_mut)), k, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView nome = texto("", 15, R.color.texto, false);
            nome.setText(sb);
            txt.addView(nome);
            String fam = e.familias.isEmpty() ? "" : " · puxa " + String.join(", ", e.familias).toLowerCase(PT_BR);
            txt.addView(texto(e.data.format(fmt) + fam, 12, R.color.texto_mut, false));
            row.addView(txt, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(texto(d == 0 ? "hoje" : "em " + d + " d", 14, breve ? R.color.fruki_laranja : R.color.texto, true));
            listaEventos.addView(row, lp);
        }
        btnEventos.setVisibility(l.size() > 3 ? View.VISIBLE : View.GONE);
        btnEventos.setText(todosEventos ? "Ver menos" : "Ver mais (" + (l.size() - 3) + ")");
    }

    // ---------- Clientes ----------

    private String vendedor() {
        Object s = spVendedor.getSelectedItem();
        return s == null ? Dados.VENDEDORES.keySet().iterator().next() : s.toString();
    }

    private List<Cliente> clientes() {
        return Dados.VENDEDORES.get(vendedor());
    }

    private String chave(int i) {
        return vendedor() + "#" + i;
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private void desenharClientes() {
        if (listaClientes == null) return;
        List<Cliente> lista = clientes();

        // filtros por segmento
        LinkedHashSet<String> segs = new LinkedHashSet<>();
        segs.add("Todos");
        for (Cliente c : lista) segs.add(c.segmento);
        if (!segs.contains(filtroSegmento)) filtroSegmento = "Todos";
        chips.setOnCheckedStateChangeListener(null);
        chips.removeAllViews();
        for (String s : segs) {
            Chip ch = new Chip(this, null, com.google.android.material.R.attr.chipStyle);
            ch.setText(s);
            ch.setCheckable(true);
            ch.setCheckedIconVisible(false);
            ch.setId(View.generateViewId());
            ch.setTag(s);
            chips.addView(ch);
            if (s.equals(filtroSegmento)) ch.setChecked(true);
        }
        chips.setOnCheckedStateChangeListener((g, ids) -> {
            if (ids.isEmpty()) return;
            filtroSegmento = (String) g.findViewById(ids.get(0)).getTag();
            listaClientes.post(this::desenharClientes);
        });

        // ranking e meta
        int total = 0;
        List<List<Recomendacao>> rankings = new ArrayList<>();
        List<int[]> escolhidos = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            List<Recomendacao> rk = MotorRecomendacao.ranking(lista.get(i), params);
            int[] ids = escolhas.get(chave(i));
            if (ids == null) {
                ids = new int[Math.min(SUGESTOES, rk.size())];
                for (int s = 0; s < ids.length; s++) ids[s] = rk.get(s).produto.id;
            }
            for (int id : ids) total += Dados.produto(id).preco();
            rankings.add(rk);
            escolhidos.add(ids);
        }
        int meta = params.metaPorCliente * lista.size();
        int pct = Math.min(100, Math.round(total * 100f / meta));
        SpannableStringBuilder mt = new SpannableStringBuilder("Meta da semana em itens novos: ");
        mt.setSpan(new StyleSpan(Typeface.BOLD), 0, mt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mt.append("R$ ").append(String.valueOf(total)).append(" de R$ ").append(String.valueOf(meta))
                .append(" (").append(String.valueOf(pct)).append("%)");
        txtMeta.setText(mt);
        progMeta.setProgress(pct);

        // indicadores
        Set<Integer> novos = new HashSet<>();
        for (int[] ids : escolhidos) for (int id : ids) novos.add(id);
        kpiClientes.setText(lista.size() + " clientes");
        kpiTicket.setText("+R$ " + Math.round(total / (float) lista.size()) + "/cliente");
        kpiNovos.setText(novos.size() + " SKUs novos");

        // lista
        String q = normalizar(busca.trim());
        listaClientes.removeAllViews();
        int visiveis = 0;
        for (int i = 0; i < lista.size(); i++) {
            Cliente c = lista.get(i);
            if (!"Todos".equals(filtroSegmento) && !c.segmento.equals(filtroSegmento)) continue;
            if (!normalizar(c.nome + " " + c.cidade).contains(q)) continue;
            visiveis++;
            listaClientes.addView(cartaoCliente(i, c, rankings.get(i), escolhidos.get(i)));
        }
        txtContagem.setText(visiveis + " de " + lista.size() + " clientes");
        if (visiveis == 0) listaClientes.addView(texto("Nenhum cliente encontrado.", 13, R.color.texto_mut, false));
    }

    private View cartaoCliente(int i, Cliente c, List<Recomendacao> rk, int[] ids) {
        boolean aberto = abertos.contains(chave(i));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_inner);
        int p = Ui.dpi(this, 12);
        card.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = Ui.dpi(this, 8);
        card.setLayoutParams(lp);

        // cabeçalho
        LinearLayout cab = new LinearLayout(this);
        cab.setGravity(Gravity.CENTER_VERTICAL);
        cab.setClickable(true);
        cab.setFocusable(true);
        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        LinearLayout l1 = new LinearLayout(this);
        l1.setGravity(Gravity.CENTER_VERTICAL);
        l1.addView(texto(c.nome, 15, R.color.texto, true));
        TextView tag = texto(c.segmento, 11, R.color.fruki_verde_medio, false);
        tag.setBackgroundResource(R.drawable.bg_tag);
        tag.setPadding(Ui.dpi(this, 8), Ui.dpi(this, 1), Ui.dpi(this, 8), Ui.dpi(this, 1));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-2, -2);
        tlp.leftMargin = Ui.dpi(this, 6);
        l1.addView(tag, tlp);
        tx.addView(l1);
        tx.addView(texto(c.cidade + " - " + c.uf + " · " + c.mix.size() + " produtos no mix · "
                + Clima.temperaturaMedia(c.uf) + "°C · umidade " + Clima.umidadeMedia(c.uf) + "%", 12, R.color.texto_mut, false));
        cab.addView(tx, new LinearLayout.LayoutParams(0, -2, 1));
        cab.addView(texto(aberto ? "▴" : "▾", 18, R.color.texto_mut, false));
        cab.setContentDescription(c.nome + (aberto ? ", recolher sugestões" : ", ver sugestões"));
        cab.setOnClickListener(v -> {
            if (abertos.contains(chave(i))) abertos.remove(chave(i));
            else { abertos.add(chave(i)); irPara(c.uf); }
            desenharClientes();
        });
        card.addView(cab);
        if (!aberto) return card;

        // sugestões
        TextView tit = texto("Oferecer agora", 13, R.color.fruki_verde, true);
        tit.setPadding(0, Ui.dpi(this, 10), 0, Ui.dpi(this, 4));
        card.addView(tit);
        String classe = Clima.classeUmidade(Clima.umidadeMedia(c.uf));
        if (classe != null) {
            card.addView(texto("Umidade " + classe + " (" + Clima.umidadeMedia(c.uf) + "%): "
                    + Clima.dicaUmidade(classe), 12, R.color.fruki_laranja, true));
        }

        List<Recomendacao> mostradas = new ArrayList<>();
        for (int s = 0; s < ids.length; s++) {
            final int slot = s;
            Recomendacao r = buscar(rk, ids[s]);
            mostradas.add(r);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundResource(R.drawable.bg_borda_topo);
            row.setPadding(0, Ui.dpi(this, 6), 0, Ui.dpi(this, 6));

            LinearLayout topo = new LinearLayout(this);
            topo.setGravity(Gravity.CENTER_VERTICAL);
            List<Recomendacao> opcoes = new ArrayList<>();
            List<String> rotulos = new ArrayList<>();
            int sel = 0;
            for (Recomendacao x : rk) {
                boolean usado = false;
                for (int id : ids) if (id == x.produto.id) usado = true;
                if (x.produto.id == ids[s] || !usado) {
                    if (x.produto.id == ids[s]) sel = opcoes.size();
                    opcoes.add(x);
                    rotulos.add(x.produto.nome + " (" + x.pontos + ")");
                }
            }
            Spinner sp = new Spinner(this);
            ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rotulos);
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(ad);
            sp.setSelection(sel, false);
            sp.setContentDescription("Sugestão " + (s + 1) + " para " + c.nome);
            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
                    int novo = opcoes.get(pos).produto.id;
                    if (novo == ids[slot]) return;
                    int[] cur = ids.clone();
                    cur[slot] = novo;
                    escolhas.put(chave(i), cur);
                    listaClientes.post(RecomendacaoActivity.this::desenharClientes);
                }
                @Override public void onNothingSelected(AdapterView<?> a) {}
            });
            topo.addView(sp, new LinearLayout.LayoutParams(0, Ui.dpi(this, 44), 1));
            TextView sc = texto(String.valueOf(r.pontos), 13, android.R.color.white, true);
            sc.setBackgroundResource(R.drawable.bg_score);
            sc.setPadding(Ui.dpi(this, 10), Ui.dpi(this, 2), Ui.dpi(this, 10), Ui.dpi(this, 2));
            topo.addView(sc);
            row.addView(topo);

            SpannableStringBuilder why = new SpannableStringBuilder(String.join(" · ", r.motivos));
            why.append(" · R$ ").append(String.valueOf(r.produto.preco())).append("/cx");
            if (s < rk.size() && ids[s] != rk.get(s).produto.id) {
                int k = why.length();
                why.append(" · ajustado por você");
                why.setSpan(new ForegroundColorSpan(Ui.cor(this, R.color.fruki_laranja)), k, why.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                why.setSpan(new StyleSpan(Typeface.BOLD), k, why.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView w = texto("", 12, R.color.texto_mut, false);
            w.setText(why);
            row.addView(w);
            card.addView(row);
        }

        // gráfico: composição da pontuação
        TextView gt = texto("Por que essas sugestões?", 13, R.color.texto, true);
        gt.setPadding(0, Ui.dpi(this, 10), 0, Ui.dpi(this, 6));
        card.addView(gt);
        PontuacaoView pv = new PontuacaoView(this);
        pv.setItens(mostradas);
        card.addView(pv, new LinearLayout.LayoutParams(-1, -2));

        MaterialButton enviar = new MaterialButton(this);
        enviar.setText("Enviar pelo Pede Fruki (WhatsApp)");
        enviar.setAllCaps(false);
        enviar.setIconResource(R.drawable.ic_chat);
        enviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.cor(this, R.color.fruki_verde)));
        enviar.setOnClickListener(v -> previaWhatsApp(c, mostradas));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.topMargin = Ui.dpi(this, 8);
        card.addView(enviar, blp);
        return card;
    }

    private static Recomendacao buscar(List<Recomendacao> rk, int id) {
        for (Recomendacao r : rk) if (r.produto.id == id) return r;
        return rk.get(0);
    }

    /** Prévia de como a sugestão chegaria ao cliente pelo Pede Fruki. Nada é enviado. */
    private void previaWhatsApp(Cliente c, List<Recomendacao> itens) {
        StringBuilder m = new StringBuilder();
        m.append("Olá, ").append(c.nome).append("! 👋\n")
                .append("Aqui é o Pede Fruki. Separamos sugestões para a sua loja nesta semana:\n\n");
        for (int i = 0; i < itens.size(); i++) {
            Recomendacao r = itens.get(i);
            m.append(i + 1).append(". ").append(r.produto.nome).append(" — R$ ")
                    .append(r.produto.preco()).append("/cx\n");
            if (r.motivos.size() > 1) m.append("   ").append(r.motivos.get(1)).append('\n');
        }
        m.append("\nResponda com o número do item para incluir no seu pedido. 🥤");

        LinearLayout fundo = new LinearLayout(this);
        fundo.setBackgroundColor(Ui.cor(this, R.color.whats_fundo));
        int p = Ui.dpi(this, 16);
        fundo.setPadding(p, p, p * 3, p);
        TextView balao = texto(m.toString(), 14, R.color.texto, false);
        balao.setBackgroundResource(R.drawable.bg_whats);
        balao.setPadding(Ui.dpi(this, 12), Ui.dpi(this, 10), Ui.dpi(this, 12), Ui.dpi(this, 10));
        fundo.addView(balao);

        new AlertDialog.Builder(this)
                .setTitle("Prévia no WhatsApp · Pede Fruki")
                .setView(fundo)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private TextView texto(String s, float sp, int cor, boolean negrito) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(Ui.cor(this, cor));
        if (negrito) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }
}
