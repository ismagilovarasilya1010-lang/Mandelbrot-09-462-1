package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.fractals.FractalSession;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Julia;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.smak.math.Complex;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.imageio.ImageIO;

public class MenuManager {
    private final FractalPainter painter;
    private final Component panel;

    public MenuManager(FractalPainter painter, Component panel) {
        this.painter = painter;
        this.panel = panel;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveItem = new JMenuItem("Сохранить как...");
        saveItem.addActionListener(e -> saveImageWithChoice());
        fileMenu.add(saveItem);

        JMenuItem saveFracItem = new JMenuItem("Сохранить как FRAC...");
        saveFracItem.addActionListener(e -> saveFractal());
        fileMenu.add(saveFracItem);

        fileMenu.addSeparator();

        JMenuItem openFracItem = new JMenuItem("Открыть FRAC...");
        openFracItem.addActionListener(e -> openFractal());
        fileMenu.add(openFracItem);

        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Правка");

        JMenuItem undoItem = new JMenuItem("Отменить");
        undoItem.addActionListener(this::showNotImplementedMessage);
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("Повторить");
        redoItem.addActionListener(this::showNotImplementedMessage);
        editMenu.add(redoItem);

        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("Вид");

        JMenu formulaMenu = new JMenu("Формулы для построения");

        ButtonGroup formulaGroup = new ButtonGroup();
        JRadioButtonMenuItem formula1 = new JRadioButtonMenuItem("Формула RGB 1 (sin/cos)");
        JRadioButtonMenuItem formula2 = new JRadioButtonMenuItem("Формула RGB 2");
        JRadioButtonMenuItem formula3 = new JRadioButtonMenuItem("Формула RGB 3");

        formula1.setSelected(true);
        formula1.addActionListener(this::showNotImplementedMessage);
        formula2.addActionListener(this::showNotImplementedMessage);
        formula3.addActionListener(this::showNotImplementedMessage);

        formulaGroup.add(formula1);
        formulaGroup.add(formula2);
        formulaGroup.add(formula3);

        formulaMenu.add(formula1);
        formulaMenu.add(formula2);
        formulaMenu.add(formula3);

        JMenu colorSchemeMenu = new JMenu("Цветовая схема");

        ButtonGroup schemeGroup = new ButtonGroup();
        JRadioButtonMenuItem scheme1 = new JRadioButtonMenuItem("Схема 1 (по умолчанию)");
        JRadioButtonMenuItem scheme2 = new JRadioButtonMenuItem("Схема 2");
        JRadioButtonMenuItem scheme3 = new JRadioButtonMenuItem("Схема 3");

        scheme1.setSelected(true);
        scheme1.addActionListener(this::showNotImplementedMessage);
        scheme2.addActionListener(this::showNotImplementedMessage);
        scheme3.addActionListener(this::showNotImplementedMessage);

        schemeGroup.add(scheme1);
        schemeGroup.add(scheme2);
        schemeGroup.add(scheme3);

        colorSchemeMenu.add(scheme1);
        colorSchemeMenu.add(scheme2);
        colorSchemeMenu.add(scheme3);

        viewMenu.add(formulaMenu);
        viewMenu.add(colorSchemeMenu);

        menuBar.add(viewMenu);

        JMenu fractalMenu = new JMenu("Фрактал");

        JMenuItem tourItem = new JMenuItem("Экскурсия по фракталу");
        tourItem.addActionListener(this::showNotImplementedMessage);
        fractalMenu.add(tourItem);

        menuBar.add(fractalMenu);

        return menuBar;
    }

    private void showNotImplementedMessage(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        JOptionPane.showMessageDialog(
                null,
                "Функция \"" + source.getText() + "\" будет реализована позже",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void saveImage(String format) {
        System.out.println("SAVE CLICKED " + format);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить изображение");

        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG Image (*.jpg, *.jpeg)", "jpg", "jpeg");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image (*.png)", "png");

        chooser.addChoosableFileFilter(jpgFilter);
        chooser.addChoosableFileFilter(pngFilter);

        if ("jpg".equals(format)) {
            chooser.setFileFilter(jpgFilter);
        } else if ("png".equals(format)) {
            chooser.setFileFilter(pngFilter);
        }

        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            String selectedFormat;
            if (chooser.getFileFilter() == jpgFilter) {
                selectedFormat = "jpg";
            } else if (chooser.getFileFilter() == pngFilter) {
                selectedFormat = "png";
            } else {
                selectedFormat = format;
            }

            String filePath = file.getPath();

            String lowerPath = filePath.toLowerCase();
            if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                filePath = filePath.substring(0, lowerPath.lastIndexOf('.'));
            } else if (lowerPath.endsWith(".png")) {
                filePath = filePath.substring(0, lowerPath.lastIndexOf('.'));
            }

            file = new File(filePath + "." + selectedFormat);

            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "Файл \"" + file.getName() + "\" уже существует.\nЗаменить его?",
                        "Подтверждение перезаписи",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                BufferedImage img = painter.createImage();

                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE);
                g.drawString(
                        String.format("Re: [%.3f; %.3f], Im: [%.3f; %.3f]",
                                painter.getConverter().getXMin(),
                                painter.getConverter().getXMax(),
                                painter.getConverter().getYMin(),
                                painter.getConverter().getYMax()
                        ),
                        10,
                        img.getHeight() - 10
                );
                g.dispose();

                ImageIO.write(img, selectedFormat, file);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Ошибка при сохранении файла:\n" + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void saveImageWithChoice() {
        saveImage(null);
    }

    private File ensureExtension(File file, String expectedExt) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith("." + expectedExt.toLowerCase())) {
            return new File(file.getParentFile(), file.getName() + "." + expectedExt);
        }
        return file;
    }

    private void saveFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить фрактал");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Файл фрактала (*.frac)", "frac");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(chooser.getSelectedFile(), "frac");

            try {
                FractalSession session = new FractalSession();
                session.xMin = painter.getConverter().getXMin();
                session.xMax = painter.getConverter().getXMax();
                session.yMin = painter.getConverter().getYMin();
                session.yMax = painter.getConverter().getYMax();

                Field fractalField = FractalPainter.class.getDeclaredField("fractal");
                fractalField.setAccessible(true);
                Fractal currentFractal = (Fractal) fractalField.get(painter);

                if (currentFractal instanceof Julia) {
                    session.type = "julia";
                    Field cField = Julia.class.getDeclaredField("c");
                    cField.setAccessible(true);
                    Complex c = (Complex) cField.get(currentFractal);
                    session.juliaCRe = c.getReal();
                    session.juliaCIm = c.getImaginary();
                } else {
                    session.type = "mandelbrot";
                }

                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    out.println("# Fractal Session v1.0");
                    out.println("type=" + session.type);
                    out.println("xMin=" + session.xMin);
                    out.println("xMax=" + session.xMax);
                    out.println("yMin=" + session.yMin);
                    out.println("yMax=" + session.yMax);
                    if (session.juliaCRe != null) {
                        out.println("juliaCRe=" + session.juliaCRe);
                        out.println("juliaCIm=" + session.juliaCIm);
                    }
                }

                JOptionPane.showMessageDialog(null, "Фрактал сохранён: " + file.getName());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка сохранения: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть фрактал");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Файл фрактала (*.frac)", "frac");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream(file)) {
                    props.load(in);
                }

                double xMin = Double.parseDouble(props.getProperty("xMin"));
                double xMax = Double.parseDouble(props.getProperty("xMax"));
                double yMin = Double.parseDouble(props.getProperty("yMin"));
                double yMax = Double.parseDouble(props.getProperty("yMax"));

                SwingUtilities.invokeLater(() -> {
                    painter.getConverter().setXShape(xMin, xMax);
                    painter.getConverter().setYShape(yMin, yMax);

                    if (panel != null) {
                        panel.repaint();
                    }
                });

                String type = props.getProperty("type");
                if ("julia".equals(type)) {
                    String cRe = props.getProperty("juliaCRe");
                    String cIm = props.getProperty("juliaCIm");
                    if (cRe != null && cIm != null) {
                        Complex newC = new Complex(Double.parseDouble(cRe), Double.parseDouble(cIm));
                        Julia newJulia = new Julia(newC);

                        Field fractalField = FractalPainter.class.getDeclaredField("fractal");
                        fractalField.setAccessible(true);
                        fractalField.set(painter, newJulia);
                    }
                }

                JOptionPane.showMessageDialog(null, "Фрактал загружен: " + file.getName());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Ошибка чтения файла: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}