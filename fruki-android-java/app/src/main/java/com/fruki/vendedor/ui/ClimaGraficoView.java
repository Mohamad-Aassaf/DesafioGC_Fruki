package com.fruki.vendedor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.fruki.vendedor.R;
import com.fruki.vendedor.motor.Clima;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Gráfico de 14 dias: chuva em barras, temperatura e umidade em linhas. */
public class ClimaGraficoView extends View {

    private static final String[] SEMANA = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};

    private List<Clima.Dia> dias = new ArrayList<>();
    private final Paint barra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linhaT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linhaU = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ponto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rotulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eixo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grade = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ClimaGraficoView(Context c) { this(c, null); }

    public ClimaGraficoView(Context c, AttributeSet a) {
        super(c, a);
        barra.setColor(Ui.cor(c, R.color.fruki_azul));
        barra.setAlpha(70);
        linhaT.setColor(Ui.cor(c, R.color.fruki_laranja));
        linhaT.setStyle(Paint.Style.STROKE);
        linhaT.setStrokeWidth(Ui.dp(c, 2.5f));
        linhaT.setStrokeJoin(Paint.Join.ROUND);
        linhaU.setColor(Ui.cor(c, R.color.fruki_verde));
        linhaU.setStyle(Paint.Style.STROKE);
        linhaU.setStrokeWidth(Ui.dp(c, 2f));
        linhaU.setPathEffect(new DashPathEffect(new float[]{Ui.dp(c, 5), Ui.dp(c, 4)}, 0));
        ponto.setColor(Ui.cor(c, R.color.fruki_laranja));
        rotulo.setColor(Ui.cor(c, R.color.texto));
        rotulo.setTextSize(Ui.sp(c, 10));
        rotulo.setTextAlign(Paint.Align.CENTER);
        eixo.setColor(Ui.cor(c, R.color.texto_mut));
        eixo.setTextSize(Ui.sp(c, 9.5f));
        eixo.setTextAlign(Paint.Align.CENTER);
        grade.setColor(Ui.cor(c, R.color.borda));
        grade.setStrokeWidth(Ui.dp(c, 1));
    }

    public void setDias(List<Clima.Dia> d) {
        dias = d;
        setContentDescription("Previsão de 14 dias com chuva, temperatura e umidade");
        invalidate();
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), Ui.dpi(getContext(), 210));
    }

    @Override
    protected void onDraw(Canvas c) {
        if (dias.isEmpty()) return;
        float topo = Ui.dp(getContext(), 22), base = getHeight() - Ui.dp(getContext(), 34);
        float alt = base - topo, w = getWidth();
        int n = dias.size();
        float slot = w / n;

        for (int g = 0; g <= 4; g++) {
            float y = topo + alt * g / 4f;
            c.drawLine(0, y, w, y, grade);
        }

        // chuva (barras, 0..100 %)
        for (int i = 0; i < n; i++) {
            float bh = alt * dias.get(i).chuva / 100f;
            float x = i * slot + slot * .22f;
            c.drawRoundRect(new RectF(x, base - bh, x + slot * .56f, base), 6, 6, barra);
        }

        // temperatura: faixa min-2 .. max+2 ocupando a parte de cima
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Clima.Dia d : dias) { min = Math.min(min, d.temperatura); max = Math.max(max, d.temperatura); }
        float tMin = min - 2, tMax = max + 2;
        Path pt = new Path(), pu = new Path();
        float[][] pontos = new float[n][2];
        for (int i = 0; i < n; i++) {
            float x = i * slot + slot / 2;
            float yt = topo + alt * .05f + (alt * .55f) * (1 - (dias.get(i).temperatura - tMin) / (tMax - tMin));
            float yu = topo + alt * (1 - dias.get(i).umidade / 100f);
            if (i == 0) { pt.moveTo(x, yt); pu.moveTo(x, yu); } else { pt.lineTo(x, yt); pu.lineTo(x, yu); }
            pontos[i][0] = x;
            pontos[i][1] = yt;
        }
        c.drawPath(pu, linhaU);
        c.drawPath(pt, linhaT);
        float r = Ui.dp(getContext(), 3);
        for (int i = 0; i < n; i++) {
            c.drawCircle(pontos[i][0], pontos[i][1], r, ponto);
            c.drawText(dias.get(i).temperatura + "°", pontos[i][0], pontos[i][1] - r * 2.2f, rotulo);
        }

        // eixo: dia da semana e dia do mês
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < n; i++) {
            LocalDate d = hoje.plusDays(i + 1);
            float x = i * slot + slot / 2;
            c.drawText(SEMANA[d.getDayOfWeek().getValue() - 1], x, base + Ui.dp(getContext(), 14), eixo);
            c.drawText(String.valueOf(d.getDayOfMonth()), x, base + Ui.dp(getContext(), 27), eixo);
        }
    }
}
