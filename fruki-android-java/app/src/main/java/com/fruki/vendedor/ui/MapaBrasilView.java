package com.fruki.vendedor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fruki.vendedor.R;
import com.fruki.vendedor.data.Dados;
import com.fruki.vendedor.motor.Clima;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mapa do Brasil por estado, colorido pela temperatura média prevista. Toque para escolher a UF. */
public class MapaBrasilView extends View {

    public interface OnEstadoListener {
        void onEstado(String uf);
    }

    private static final float LADO = 395f;

    private final Map<String, Path> base = new LinkedHashMap<>();
    private final Map<String, float[]> centros = new LinkedHashMap<>();
    private final Map<String, Path> telas = new LinkedHashMap<>();
    private final Map<String, Region> regioes = new LinkedHashMap<>();
    private final Paint preench = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint contorno = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float escala = 1, dx = 0;
    private String selecionado = "RS";
    private OnEstadoListener listener;

    public MapaBrasilView(Context c) { this(c, null); }

    public MapaBrasilView(Context c, AttributeSet a) {
        super(c, a);
        for (Map.Entry<String, String> e : Dados.CONTORNOS.entrySet()) {
            Path p = new Path();
            float sx = 0, sy = 0;
            String[] pts = e.getValue().split(" ");
            for (int i = 0; i < pts.length; i++) {
                String[] xy = pts[i].split(",");
                float x = (Float.parseFloat(xy[0]) + 74) * 9.7f;
                float y = (5.3f - Float.parseFloat(xy[1])) * 10f;
                if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
                sx += x;
                sy += y;
            }
            p.close();
            base.put(e.getKey(), p);
            centros.put(e.getKey(), new float[]{sx / pts.length, sy / pts.length});
        }
        contorno.setStyle(Paint.Style.STROKE);
        contorno.setStrokeJoin(Paint.Join.ROUND);
        texto.setColor(Color.WHITE);
        texto.setTextAlign(Paint.Align.CENTER);
        texto.setFakeBoldText(true);
        setContentDescription("Mapa do Brasil por estado. Toque em um estado para ver a previsão.");
    }

    public void setOnEstadoListener(OnEstadoListener l) { listener = l; }

    public void setSelecionado(String uf) {
        selecionado = uf;
        invalidate();
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int max = Ui.dpi(getContext(), 420);
        int lado = Math.min(w, max);
        setMeasuredDimension(w, lado);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        escala = Math.min(w, h) / LADO;
        dx = (w - LADO * escala) / 2f;
        Matrix m = new Matrix();
        m.setScale(escala, escala);
        m.postTranslate(dx, 0);
        telas.clear();
        regioes.clear();
        Region clip = new Region(0, 0, w, h);
        for (Map.Entry<String, Path> e : base.entrySet()) {
            Path p = new Path();
            e.getValue().transform(m, p);
            telas.put(e.getKey(), p);
            Region r = new Region();
            r.setPath(p, clip);
            regioes.put(e.getKey(), r);
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        texto.setTextSize(7.2f * escala);
        for (String uf : telas.keySet()) if (!uf.equals(selecionado)) desenhar(c, uf, false);
        if (telas.containsKey(selecionado)) desenhar(c, selecionado, true);
    }

    private void desenhar(Canvas c, String uf, boolean sel) {
        Path p = telas.get(uf);
        preench.setColor(Clima.corTemperatura(Clima.temperaturaMedia(uf)));
        c.drawPath(p, preench);
        contorno.setColor(sel ? Ui.cor(getContext(), R.color.fruki_verde_escuro) : Color.WHITE);
        contorno.setStrokeWidth(sel ? 2.4f * escala : 0.8f * escala);
        c.drawPath(p, contorno);
        float[] ct = centros.get(uf);
        float x = ct[0] * escala + dx, y = ct[1] * escala;
        c.drawText(uf, x, y - (texto.descent() + texto.ascent()) / 2, texto);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            String achado = null;
            float menor = Float.MAX_VALUE;
            RectF b = new RectF();
            for (Map.Entry<String, Region> e : regioes.entrySet()) {
                if (e.getValue().contains((int) ev.getX(), (int) ev.getY())) {
                    telas.get(e.getKey()).computeBounds(b, true);
                    float area = b.width() * b.height();
                    if (area < menor) { menor = area; achado = e.getKey(); }
                }
            }
            if (achado != null) {
                setSelecionado(achado);
                if (listener != null) listener.onEstado(achado);
                performClick();
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
