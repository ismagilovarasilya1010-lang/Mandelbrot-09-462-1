package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

public class Julia extends Mandelbrot {
    private final Complex c;

    public Julia(Complex c) {
        this.c = c;
    }

    public void setDynamicIterations(DynamicIterations di) {
        super.setDynamicIterations(di);
    }

    @Override
    public float inSetProbability(double x, double y) {
        var z = new Complex(x, y);

        int currentMax = getCurrentMaxIterations();

        int i = 0;
        while (z.getAbsoluteValue2() < 4 && ++i < currentMax) {
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / currentMax;
    }
}