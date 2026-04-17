package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.DynamicIterations;
import ru.gr0946x.ui.fractals.Julia;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.smak.math.Complex;

import javax.swing.*;
import java.awt.*;

public class JuliaWindow extends JFrame {
    private final DynamicIterations dynamicIter;
    private final SelectablePanel juliaPanel;

    public JuliaWindow(Complex c, String title, DynamicIterations sharedDynamicIter) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        setTitle(title + " (c = " + String.format("%.3f", c.getReal()) + " + " + String.format("%.3f", c.getImaginary()) + "i)");

        this.dynamicIter = sharedDynamicIter;

        Converter conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        dynamicIter.syncLastWidth(conv.getXMax() - conv.getXMin());

        var julia = new Julia(c);
        julia.setDynamicIterations(dynamicIter);

        var painter = new FractalPainter(julia, conv, (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float) Math.abs(Math.sin(5 * value));
            var g = (float) Math.abs(Math.cos(8 * value) * Math.sin(3 * value));
            var b = (float) Math.abs((Math.sin(7 * value) + Math.cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });

        juliaPanel = new SelectablePanel(painter, conv);
        juliaPanel.setBackground(Color.WHITE);
        juliaPanel.setDynamicIterations(dynamicIter);

        juliaPanel.addSelectListener((r) -> {
            if (r.width < 10 || r.height < 10) {
                return;
            }

            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            juliaPanel.applyZoom(xMin, xMax, yMin, yMax);
        });

        MenuManager menuManager = new MenuManager(painter, juliaPanel, null);
        menuManager.setJuliaMode(true);
        setJMenuBar(menuManager.createMenuBar());

        JMenu viewMenu = null;
        for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
            JMenu menu = getJMenuBar().getMenu(i);
            if ("Вид".equals(menu.getText())) {
                viewMenu = menu;
                break;
            }
        }
        if (viewMenu != null) {
            viewMenu.addSeparator();
            JCheckBoxMenuItem dynamicIterItem = new JCheckBoxMenuItem("Динамическое число итераций");
            dynamicIterItem.setSelected(dynamicIter.isEnabled());
            dynamicIterItem.addActionListener(e -> {
                boolean enabled = dynamicIterItem.isSelected();
                dynamicIter.setEnabled(enabled);
                if (enabled) {
                    double width = conv.getXMax() - conv.getXMin();
                    dynamicIter.updateAndGetIterations(width);
                }
                juliaPanel.repaint();
            });
            viewMenu.add(dynamicIterItem);
        }

        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(juliaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(juliaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );

        SwingUtilities.invokeLater(() -> juliaPanel.repaint());
    }
}