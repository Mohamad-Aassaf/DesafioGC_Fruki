package com.fruki.vendedor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fruki.vendedor.R;

import java.util.ArrayList;
import java.util.List;

/** Colunas verticais ordenadas: a maior em verde, a menor em laranja. Toque abre a coluna. */
public class BarrasView extends View {

    public interface OnBarraListener {
        void onBarra(String nome);
    }

    private final List<String> nomes = new ArrayList<>();
    private final List<Integer> valores = new ArrayList<>();
    private boolean clicavel;
    private OnBarraListener listener;
    private final Paint barra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pct = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rot = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BarrasView(Context c) { this(c, null); }

    public BarrasView(Context c, AttributeSet a) {
        super(c, a);
        pct.setColor(Ui.cor(c, R.color.texto));
        pct.setTextSize(Ui.sp(c, 12));
        pct.setFakeBoldText(true);
        pct.setTextAlign(Paint.Align.CENTER);
        rot.setColor(Ui.cor(c, R.color.texto));
        rot.setTextSize(Ui.sp(c, 10.5f));
        rot.setTextAlign(Paint.Align.CENTER);
    }

    public void setOnBarraListener(OnBarraListener l) { listener = l; }

    public void setDados(List<String> n, List<Integer> v, boolean clicavel) {
        nomes.clear(); nomes.addAll(n);
        valores.clear(); valores.addAll(v);
        this.clicavel = clicavel;
        StringBuilder sb = new StringBuilder("Gráfico de colunas: ");
        for (int i = 0; i < n.size(); i++) sb.append(n.get(i)).append(' ').append(v.get(i)).append(". ");
        setContentDescription(sb);
        invalidate();
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), Ui.dpi(getContext(), 240));
    }

    @Override
    protected void onDraw(Canvas c) {
        int n = valores.size();
        if (n == 0) return;
        int max = 0, tot = 0;
        for (int v : valores) { max = Math.max(max, v); tot += v; }
        float slot = getWidth() / (float) n;
        float topo = Ui.dp(getContext(), 20), base = getHeight() - Ui.dp(getContext(), 40);
        int verde = Ui.cor(getContext(), R.color.fruki_verde);
        int laranja = Ui.cor(getContext(), R.color.fruki_laranja);
        for (int i = 0; i < n; i++) {
            float h = (base - topo) * valores.get(i) / max;
            float bw = Math.min(slot * .62f, Ui.dp(getContext(), 56));
            float x = i * slot + (slot - bw) / 2;
            barra.setColor(i == n - 1 ? laranja : verde);
            barra.setAlpha(i == 0 || i == n - 1 ? 255 : 150);
            float rr = Ui.dp(getContext(), 8);
            c.drawRoundRect(new RectF(x, base - h, x + bw, base + rr), rr, rr, barra);
            c.drawText(Math.round(valores.get(i) * 100f / tot) + "%", i * slot + slot / 2, base - h - Ui.dp(getContext(), 6), pct);
            desenharRotulo(c, nomes.get(i), i * slot + slot / 2, base + Ui.dp(getContext(), 14), slot - Ui.dp(getContext(), 4));
        }
    }

    /** Quebra o nome em até duas linhas que caibam na coluna. */
    private void desenharRotulo(Canvas c, String s, float x, float y, float largura) {
        String[] palavras = s.split(" ");
        StringBuilder l1 = new StringBuilder(), l2 = new StringBuilder();
        for (String p : palavras) {
            String teste = l1.length() == 0 ? p : l1 + " " + p;
            if (l2.length() == 0 && rot.measureText(teste) <= largura) l1 = new StringBuilder(teste);
            else l2.append(l2.length() == 0 ? "" : " ").append(p);
        }
        c.drawText(l1.toString(), x, y, rot);
        if (l2.length() > 0) c.drawText(l2.toString(), x, y + rot.getTextSize() * 1.15f, rot);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!clicavel || valores.isEmpty()) return super.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            int i = (int) (ev.getX() / (getWidth() / (float) valores.size()));
            i = Math.max(0, Math.min(valores.size() - 1, i));
            if (listener != null) listener.onBarra(nomes.get(i));
            performClick();
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
