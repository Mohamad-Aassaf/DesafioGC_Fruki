package com.fruki.vendedor.ui;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Pequenos utilitários de interface. */
public final class Ui {
    private Ui() {}

    public static float dp(Context c, float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    public static int dpi(Context c, float v) {
        return Math.round(dp(c, v));
    }

    public static float sp(Context c, float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, c.getResources().getDisplayMetrics());
    }

    public static int cor(Context c, int res) {
        return ContextCompat.getColor(c, res);
    }

    /** Desenha atrás das barras do sistema e afasta o conteúdo delas com padding. */
    public static void bordaABorda(Activity a, View raiz) {
        WindowCompat.setDecorFitsSystemWindows(a.getWindow(), false);
        WindowInsetsControllerCompat ctl = WindowCompat.getInsetsController(a.getWindow(), raiz);
        ctl.setAppearanceLightStatusBars(true);
        ctl.setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(b.left, b.top, b.right, b.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
