package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.fractals.FractalSession;
import ru.gr0946x.ui.fractals.Julia;
import ru.gr0946x.ui.fractals.FractalConfig;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.smak.math.Complex;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.imageio.ImageIO;

public class MenuManager {
    private final FractalPainter painter;
    private final SelectablePanel panel;
    private final MainWindow mainWindow;
    private boolean juliaMode = false;

    public MenuManager(FractalPainter painter, SelectablePanel panel, MainWindow mainWindow) {
        this.painter = painter;
        this.panel = panel;
        this.mainWindow = mainWindow;
        this.juliaMode = (mainWindow == null);
    }

    public void setJuliaMode(boolean juliaMode) {
        this.juliaMode = juliaMode;
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
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> panel.undo());
        editMenu.add(undoItem);
        JMenuItem redoItem = new JMenuItem("Повторить");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> panel.redo());
        editMenu.add(redoItem);
        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("Вид");

        if (!juliaMode) {
            JMenu formulaMenu = new JMenu("Формулы для построения");
            ButtonGroup formulaGroup = new ButtonGroup();
            for (int i = 0; i < FractalConfig.FRACTAL_NAMES.size(); i++) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(FractalConfig.FRACTAL_NAMES.get(i));
                final int idx = i;
                item.addActionListener(e -> {
                    if (item.isSelected() && mainWindow != null) {
                        mainWindow.applySettings(idx, mainWindow.getCurrentColorIdx());
                    }
                });
                formulaGroup.add(item);
                formulaMenu.add(item);
            }
            if (formulaGroup.getButtonCount() > 0) {
                formulaGroup.getElements().nextElement().setSelected(true);
            }
            viewMenu.add(formulaMenu);
        }

        JMenu colorSchemeMenu = new JMenu("Цветовая схема");
        ButtonGroup schemeGroup = new ButtonGroup();
        for (int i = 0; i < FractalConfig.COLOR_NAMES.size(); i++) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(FractalConfig.COLOR_NAMES.get(i));
            final int idx = i;
            item.addActionListener(e -> {
                if (item.isSelected()) {
                    if (juliaMode) {
                        painter.setColorFunction(FractalConfig.COLORS.get(idx));
                        panel.repaint();
                    } else {
                        mainWindow.applySettings(mainWindow.getCurrentFractalIdx(), idx);
                    }
                }
            });
            schemeGroup.add(item);
            colorSchemeMenu.add(item);
        }
        if (schemeGroup.getButtonCount() > 0) {
            schemeGroup.getElements().nextElement().setSelected(true);
        }
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
        JOptionPane.showMessageDialog(null,
                "Функция \"" + source.getText() + "\" будет реализована позже",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveImage(String format) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить изображение");
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG Image (*.jpg, *.jpeg)", "jpg", "jpeg");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image (*.png)", "png");
        chooser.addChoosableFileFilter(jpgFilter);
        chooser.addChoosableFileFilter(pngFilter);
        if ("jpg".equals(format)) chooser.setFileFilter(jpgFilter);
        else if ("png".equals(format)) chooser.setFileFilter(pngFilter);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String selectedFormat;
            if (chooser.getFileFilter() == jpgFilter) selectedFormat = "jpg";
            else if (chooser.getFileFilter() == pngFilter) selectedFormat = "png";
            else selectedFormat = format;

            String filePath = file.getPath().toLowerCase();
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png")) {
                filePath = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
            }
            file = new File(filePath + "." + selectedFormat);

            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(null,
                        "Файл \"" + file.getName() + "\" уже существует.\nЗаменить его?",
                        "Подтверждение перезаписи", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return;
            }

            try {
                BufferedImage img = painter.createImage();
                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE);
                g.drawString(String.format("Re: [%.3f; %.3f], Im: [%.3f; %.3f]",
                                painter.getConverter().getXMin(), painter.getConverter().getXMax(),
                                painter.getConverter().getYMin(), painter.getConverter().getYMax()),
                        10, img.getHeight() - 10);
                g.dispose();
                ImageIO.write(img, selectedFormat, file);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка при сохранении файла:\n" + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveImageWithChoice() { saveImage(null); }

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

                if (juliaMode) {
                    session.type = "julia";
                    if (painter instanceof FractalPainter) {
                        java.lang.reflect.Field cField = Julia.class.getDeclaredField("c");
                        cField.setAccessible(true);
                        Complex c = (Complex) cField.get(((FractalPainter) painter).getFractal());
                        session.juliaCRe = c.getReal();
                        session.juliaCIm = c.getImaginary();
                    }
                    session.fractalIdx = 0;
                    session.colorIdx = findColorIndex(painter.getColorFunction());
                } else {
                    session.type = "mandelbrot";
                    session.fractalIdx = mainWindow.getCurrentFractalIdx();
                    session.colorIdx = mainWindow.getCurrentColorIdx();
                }

                if (panel instanceof SelectablePanel) {
                    java.lang.reflect.Field diField = SelectablePanel.class.getDeclaredField("dynamicIterations");
                    diField.setAccessible(true);
                    ru.gr0946x.ui.fractals.DynamicIterations di =
                            (ru.gr0946x.ui.fractals.DynamicIterations) diField.get(panel);
                    if (di != null) {
                        session.dynamicIterationsEnabled = di.isEnabled();
                        session.dynamicIterationsLastWidth = di.getCurrentIterations();
                    }
                }

                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    out.println("# Fractal Session v1.0");
                    out.println("type=" + session.type);
                    out.println("fractalIdx=" + session.fractalIdx);
                    out.println("colorIdx=" + session.colorIdx);
                    out.println("xMin=" + session.xMin);
                    out.println("xMax=" + session.xMax);
                    out.println("yMin=" + session.yMin);
                    out.println("yMax=" + session.yMax);
                    if (session.juliaCRe != null) {
                        out.println("juliaCRe=" + session.juliaCRe);
                        out.println("juliaCIm=" + session.juliaCIm);
                    }
                    out.println("dynamicIterationsEnabled=" + session.dynamicIterationsEnabled);
                    out.println("dynamicIterationsLastWidth=" + session.dynamicIterationsLastWidth);
                }

                JOptionPane.showMessageDialog(null, "Фрактал сохранён: " + file.getName());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка сохранения: " + e.getMessage(),
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

                String type = props.getProperty("type");
                int fractalIdx = Integer.parseInt(props.getProperty("fractalIdx", "0"));
                int colorIdx = Integer.parseInt(props.getProperty("colorIdx", "0"));
                double xMin = Double.parseDouble(props.getProperty("xMin"));
                double xMax = Double.parseDouble(props.getProperty("xMax"));
                double yMin = Double.parseDouble(props.getProperty("yMin"));
                double yMax = Double.parseDouble(props.getProperty("yMax"));
                boolean dynIterEnabled = Boolean.parseBoolean(props.getProperty("dynamicIterationsEnabled", "false"));
                double dynIterWidth = Double.parseDouble(props.getProperty("dynamicIterationsLastWidth", "4.0"));

                SwingUtilities.invokeLater(() -> {
                    try {

                        if ("julia".equals(type)) {
                            String cRe = props.getProperty("juliaCRe");
                            String cIm = props.getProperty("juliaCIm");
                            if (cRe != null && cIm != null) {
                                Complex newC = new Complex(Double.parseDouble(cRe), Double.parseDouble(cIm));
                                Julia newJulia = new Julia(newC);

                                if (painter instanceof FractalPainter) {
                                    ((FractalPainter) painter).setFractal(newJulia);
                                    ((FractalPainter) painter).setColorFunction(FractalConfig.COLORS.get(colorIdx));
                                }

                                painter.getConverter().setXShape(xMin, xMax);
                                painter.getConverter().setYShape(yMin, yMax);

                                if (panel instanceof SelectablePanel) {
                                    java.lang.reflect.Field diField = SelectablePanel.class.getDeclaredField("dynamicIterations");
                                    diField.setAccessible(true);
                                    ru.gr0946x.ui.fractals.DynamicIterations di =
                                            (ru.gr0946x.ui.fractals.DynamicIterations) diField.get(panel);
                                    if (di != null) {
                                        di.setEnabled(dynIterEnabled);
                                        di.syncLastWidth(dynIterWidth);
                                    }
                                }
                            }
                            if (panel != null) panel.repaint();

                        } else {
                            if (mainWindow != null) {
                                FractalSession session = new FractalSession(
                                        "mandelbrot", fractalIdx, colorIdx,
                                        xMin, xMax, yMin, yMax,
                                        null, dynIterEnabled, dynIterWidth
                                );
                                mainWindow.restoreFromSession(session);
                            }
                        }

                        JOptionPane.showMessageDialog(null, "Фрактал загружен: " + file.getName());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Ошибка восстановления: " + ex.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка чтения файла: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int findColorIndex(ColorFunction cf) {
        for (int i = 0; i < FractalConfig.COLORS.size(); i++) {
            if (FractalConfig.COLORS.get(i) == cf) {
                return i;
            }
        }
        return 0;
    }
}