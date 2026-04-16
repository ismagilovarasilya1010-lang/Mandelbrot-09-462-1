package ru.gr0946x.ui.fractals;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

public class FractalConfig {
    public static final List<String> FRACTAL_NAMES = new ArrayList<>();
    public static final List<Fractal> FRACTALS = new ArrayList<>();
    public static final List<String> COLOR_NAMES = new ArrayList<>();
    public static final List<ColorFunction> COLORS = new ArrayList<>();

    static {
        FRACTAL_NAMES.add("Формула 1 (Классика z^2+c)");
        FRACTALS.add(new Mandelbrot());

        FRACTAL_NAMES.add("Формула 2 (Куб z^3+c)");
        FRACTALS.add((x, y) -> {
            int m = 100; double r2 = 4, zx = x, zy = y, i = 0;
            while (zx*zx + zy*zy < r2 && ++i < m) {
                double t = zx*zx*zx - 3*zx*zy*zy + x;
                zy = 3*zx*zx*zy - zy*zy*zy + y; zx = t;
            } return (float)i / m;
        });

        FRACTAL_NAMES.add("Формула 3 (Со смещением)");
        FRACTALS.add((x, y) -> {
            int m = 100; double r2 = 4, zx = x, zy = y, i = 0;
            while (zx*zx + zy*zy < r2 && ++i < m) {
                double t = zx*zx - zy*zy + x + 0.5;
                zy = 2*zx*zy + y + 0.3; zx = t;
            } return (float)i / m;
        });

        FRACTAL_NAMES.add("Формула 4 (Степень 4)");
        FRACTALS.add((x, y) -> {
            int m = 100; double r2 = 16, zx = x, zy = y, i = 0;
            while (zx*zx + zy*zy < r2 && ++i < m) {
                double a = zx*zx, b = zy*zy, t = a*a - 6*a*b + b*b + x;
                zy = 4*zx*a*zy - 4*zx*zy*b + y; zx = t;
            } return (float)i / m;
        });

        FRACTAL_NAMES.add("Формула 5 (Модифицированная)");
        FRACTALS.add((x, y) -> {
            int m = 100; double r2 = 4, zx = x, zy = y, i = 0;
            while (zx*zx + zy*zy < r2 && ++i < m) {
                double abs = Math.sqrt(zx*zx + zy*zy);
                double t = zx*zx - zy*zy + x + 0.1*abs;
                zy = 2*zx*zy + y + 0.1*abs; zx = t;
            } return (float)i / m;
        });

        COLOR_NAMES.add("Схема 1 (RGB sin/cos)");
        COLORS.add(v -> v == 1.0 ? Color.BLACK : new Color((float)abs(sin(5*v)), (float)abs(cos(8*v)*sin(3*v)), (float)abs((sin(7*v)+cos(15*v))/2f)));

        COLOR_NAMES.add("Схема 2 (Чёрно-белая)");
        COLORS.add(v -> v == 1.0 ? Color.BLACK : new Color((int)(v*255), (int)(v*255), (int)(v*255)));

        COLOR_NAMES.add("Схема 3 (Розово -> Фиолетовая)");
        COLORS.add(v -> v == 1.0 ? Color.BLACK : new Color((int)(255 - 185 * Math.pow(v, 0.9) + 18 * Math.sin(v * 12)), (int)(200 * Math.pow(1 - v, 3) * (0.6 + 0.4 * Math.sin(v * 8 + 1))), (int)(205 + 50 * Math.sin(v * 10 + 1.5) - 70 * Math.pow(v, 1.3))));

        COLOR_NAMES.add("Схема 4 (Неон)");
        COLORS.add(v -> v == 1.0 ? Color.BLACK : Color.getHSBColor(v, 0.8f, 0.9f));

        COLOR_NAMES.add("Схема 5 (Пастельная)");
        COLORS.add(v -> v == 1.0 ? Color.BLACK : new Color((int)(200 + 55 * Math.sin(v * 6.28)), (int)(200 + 45 * Math.sin(v * 6.28 + 2.1)), (int)(220 + 35 * Math.sin(v * 6.28 + 4.2))));
    }
}