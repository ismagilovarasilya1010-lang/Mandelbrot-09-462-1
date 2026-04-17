package ru.gr0946x.ui.painting;
import java.awt.image.BufferedImage;
import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;

import java.awt.*;

public class FractalPainter implements Painter{

    private Fractal fractal;
    private final Converter conv;
    private ColorFunction colorFunction;
    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
    }

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf){
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
    }

    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                var x = conv.xScr2Crt(i);
                var y = conv.yScr2Crt(j);
                var res = fractal.inSetProbability(x, y);
                g.setColor(colorFunction.getColor(res));
                g.fillRect(i, j, 1, 1);
            }
        }
    }

    public BufferedImage createImage() {
        int w = getWidth();
        int h = getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int procs = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[procs];

        for (int k = 0; k < procs; k++) {
            final int threadId = k;

            threads[k] = new Thread(() -> {
                for (int i = threadId; i < w; i += procs) {
                    for (int j = 0; j < h; j++) {
                        var x = conv.xScr2Crt(i);
                        var y = conv.yScr2Crt(j);
                        var res = fractal.inSetProbability(x, y);
                        Color color = colorFunction.getColor(res);
                        result.setRGB(i, j, color.getRGB());
                    }
                }
            });
            threads[k].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public Converter getConverter() {
        return conv;
    }
    public void setColorFunction(ColorFunction colorFunction) { this.colorFunction = colorFunction; }
    public Fractal getFractal() { return fractal; }
    public ColorFunction getColorFunction() { return colorFunction; }
    public void setFractal(Fractal fractal) {
        this.fractal = fractal;
    }
}