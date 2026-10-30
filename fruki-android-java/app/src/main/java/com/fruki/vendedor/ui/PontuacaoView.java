package com.fruki.vendedor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.fruki.vendedor.R;
import com.fruki.vendedor.motor.MotorRecomendacao;
import com.fruki.vendedor.motor.MotorRecomendacao.Recomendacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Barras empilhadas: quanto cada critério somou na pontuação de cada sugestão. */
public class PontuacaoView extends View {

    private final List<Recomendacao> itens = new ArrayList<>();
    private final Map<String, Integer> cores = new LinkedHashMap<>();
    private final Paint seg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nome = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint total = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint leg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fundo = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PontuacaoView(Context c) { this(c, null); }

    public PontuacaoView(Context c, AttributeSet a) {
        super(c, a);
        cores.put(MotorRecomendacao.HISTORICO, Ui.cor(c, R.color.fruki_verde_escuro));
        cores.put(MotorRecomendacao.CLIMA, Ui.cor(c, R.color.fruki_laranja));
        cores.put(MotorRecomendacao.UMIDADE, Ui.cor(c, R.color.fruki_azul));
        cores.put(MotorRecomendacao.EVENTO, Ui.cor(c, R.color.fruki_amarelo));
        cores.put(MotorRecomendacao.AJUSTE, Ui.cor(c, R.color.fruki_vermelho));
        nome.setColor(Ui.cor(c, R.color.texto));
        nome.setTextSize(Ui.sp(c, 12));
        total.set(nome);
        total.setFakeBoldText(true);
        total.setTextAlign(Paint.Align.RIGHT);
        leg.setColor(Ui.cor(c, R.color.texto_mut));
        leg.setTextSize(Ui.sp(c, 11));
        fundo.setColor(Ui.cor(c, R.color.trilho));
    }

    public void setItens(List<Recomendacao> r) {
        itens.clear();
        itens.addAll(r);
        StringBuilder sb = new StringBuilder("Composição da pontuação. ");
        for (Recomendacao x : r) sb.append(x.produto.nome).append(": ").append(x.pontos).append(" pontos. ");
        setContentDescription(sb);
        requestLayout();
        invalidate();
    }

    private float alturaLegenda(float w) {
        float x = 0, linhas = 1, q = Ui.dp(getContext(), 10), gap = Ui.dp(getContext(), 14);
        for (String k : cores.keySet()) {
            float lw = q + Ui.dp(getContext(), 5) + leg.measureText(k) + gap;
            if (x + lw > w) { linhas++; x = 0; }
            x += lw;
        }
        return linhas * Ui.dp(getContext(), 18) + Ui.dp(getContext(), 6);
    }

    @Override
    protected void onMeasure(int w, int h) {
        int lw = MeasureSpec.getSize(w);
        float alt = alturaLegenda(lw) + itens.size() * Ui.dp(getContext(), 42);
        setMeasuredDimension(lw, Math.round(alt));
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth();
        float q = Ui.dp(getContext(), 10), gap = Ui.dp(getContext(), 14);
        float x = 0, y = Ui.dp(getContext(), 4);
        for (Map.Entry<String, Integer> e : cores.entrySet()) {
            float lw = q + Ui.dp(getContext(), 5) + leg.measureText(e.getKey()) + gap;
            if (x + lw > w) { x = 0; y += Ui.dp(getContext(), 18); }
            seg.setColor(e.getValue());
            c.drawRoundRect(new RectF(x, y + 2, x + q, y + 2 + q), 3, 3, seg);
            c.drawText(e.getKey(), x + q + Ui.dp(getContext(), 5), y + q + 1, leg);
            x += lw;
        }
        y = alturaLegenda(w);

        int maxPos = 1;
        for (Recomendacao r : itens) {
            int pos = 0;
            for (int v : r.partes.values()) if (v > 0) pos += v;
            maxPos = Math.max(maxPos, pos);
        }
        float barH = Ui.dp(getContext(), 12), rr = Ui.dp(getContext(), 6);
        for (Recomendacao r : itens) {
            c.drawText(r.produto.nome, 0, y + Ui.dp(getContext(), 13), nome);
            c.drawText(r.pontos + " pts", w, y + Ui.dp(getContext(), 13), total);
            float by = y + Ui.dp(getContext(), 20);
            c.drawRoundRect(new RectF(0, by, w, by + barH), rr, rr, fundo);
            float bx = 0;
            for (Map.Entry<String, Integer> e : r.partes.entrySet()) {
                if (e.getValue() <= 0) continue;
                float sw = w * e.getValue() / maxPos;
                seg.setColor(cores.get(e.getKey()));
                c.drawRect(bx, by, bx + sw, by + barH, seg);
                bx += sw;
            }
            Integer aj = r.partes.get(MotorRecomendacao.AJUSTE);
            if (aj != null && aj < 0) {
                float sw = w * -aj / maxPos;
                seg.setColor(cores.get(MotorRecomendacao.AJUSTE));
                seg.setAlpha(170);
                c.drawRect(bx - sw, by, bx, by + barH, seg);
                seg.setAlpha(255);
            }
            y += Ui.dp(getContext(), 42);
        }
    }
}
