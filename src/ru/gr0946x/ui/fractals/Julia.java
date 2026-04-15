package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

public class Julia extends Mandelbrot {
    private final Complex c;

    private final int maxIterations = 100;
    private final double R2 = 4;

    public Julia(Complex c) {
        this.c = c;
    }

    @Override
    public float inSetProbability(double x, double y) {

        var z = new Complex(x, y);
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);      // z = z^2
            z.plusAssign(c);       // z = z^2 + c
        }
        return (float) i / maxIterations;
    }
}