package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;
import java.io.Serializable;

public class FractalSession implements Serializable {
    private static final long serialVersionUID = 1L;

    public String type;

    public double xMin, xMax, yMin, yMax;

    public Double juliaCRe, juliaCIm;

    public FractalSession() {}

    public FractalSession(String type, double xMin, double xMax, double yMin, double yMax, Complex juliaC) {
        this.type = type;
        this.xMin = xMin; this.xMax = xMax; this.yMin = yMin; this.yMax = yMax;
        if (juliaC != null) {
            this.juliaCRe = juliaC.getReal();
            this.juliaCIm = juliaC.getImaginary();
        }
    }

    public Complex getJuliaC() {
        if (juliaCRe != null && juliaCIm != null) {
            return new Complex(juliaCRe, juliaCIm);
        }
        return null;
    }
}