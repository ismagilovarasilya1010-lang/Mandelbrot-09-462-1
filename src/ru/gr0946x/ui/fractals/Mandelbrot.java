package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal {

    private int maxIterations = 100;
    private final double R2 = 4;

    private DynamicIterations dynamicIterations = null;

    public void setDynamicIterations(DynamicIterations di) {
        this.dynamicIterations = di;
    }

    protected int getCurrentMaxIterations() {
        if (dynamicIterations != null && dynamicIterations.isEnabled()) {
            return dynamicIterations.getCurrentIterations();
        }
        return maxIterations;
    }

    @Override
    public float inSetProbability(double x, double y) {
        var c = new Complex(x, y);
        var z = new Complex();

        int currentMax = getCurrentMaxIterations();

        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < currentMax){
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / currentMax;
    }
}