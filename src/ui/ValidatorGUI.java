package ui;

import parser.PseudoParserDriver;
import parser.PseudoValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ValidatorGUI extends JFrame {
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea reportArea = new JTextArea();
    private Path currentFile = null;

    public ValidatorGUI() {
        setTitle("PseudoValidator - Interfaz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(8,8));
        content.setBorder(new EmptyBorder(8,8,8,8));
        setContentPane(content);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JButton openBtn = new JButton("Abrir archivo...");
        JButton analyzeBtn = new JButton("Analizar");
        JButton saveReportBtn = new JButton("Guardar reporte...");
        JButton clearBtn = new JButton("Limpiar");
        top.add(openBtn);
        top.add(analyzeBtn);
        top.add(saveReportBtn);
        top.add(clearBtn);
        content.add(top, BorderLayout.NORTH);

        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inScroll = new JScrollPane(inputArea);
        inScroll.setBorder(BorderFactory.createTitledBorder("Contenido del archivo (entrada)"));

        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);
        JScrollPane outScroll = new JScrollPane(reportArea);
        outScroll.setBorder(BorderFactory.createTitledBorder("Reporte"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(inScroll);
        split.setRightComponent(outScroll);
        split.setResizeWeight(0.5);
        content.add(split, BorderLayout.CENTER);

        openBtn.addActionListener(e -> onOpen());
        analyzeBtn.addActionListener(e -> onAnalyze());
        saveReportBtn.addActionListener(e -> onSaveReport());
        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            reportArea.setText("");
            currentFile = null;
            setTitle("PseudoValidator - Interfaz");
        });

        InputMap im = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = content.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "open");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "analyze");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        am.put("open", new AbstractAction() { public void actionPerformed(ActionEvent e) { onOpen(); }});
        am.put("analyze", new AbstractAction() { public void actionPerformed(ActionEvent e) { onAnalyze(); }});
        am.put("save", new AbstractAction() { public void actionPerformed(ActionEvent e) { onSaveReport(); }});
    }

    private void onOpen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir archivo pseudo");
        int sel = fc.showOpenDialog(this);
        if (sel == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                String txt = Files.readString(f.toPath());
                inputArea.setText(txt);
                currentFile = f.toPath();
                setTitle("PseudoValidator - " + f.getName());
                reportArea.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error leyendo archivo:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAnalyze() {
        String input = inputArea.getText();
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El contenido está vacío. Abre un archivo o pega el texto.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            PseudoParserDriver.resetAll();
        } catch (Throwable ignored) {}

        PseudoValidator validator = new PseudoValidator();
        try {
            validator.parse(input);
        } catch (Throwable ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            reportArea.setText("Error al ejecutar el validador:\n" + sw.toString());
            return;
        }

        try {
            String report = PseudoParserDriver.getReport();
            // si quieres incluir archivo en el reporte cuando exista:
            if (currentFile != null && !currentFile.getFileName().toString().isEmpty()) {
                report = report.replaceFirst("--- REPORTE DE VALIDACIÓN ---", 
                        "--- REPORTE DE VALIDACIÓN ---\nArchivo analizado: " + currentFile.getFileName());
            }
            reportArea.setText(report);
            reportArea.setCaretPosition(0);
        } catch (Throwable ex) {
            reportArea.setText("No se pudo obtener el reporte: " + ex.getMessage());
        }
    }

    private void onSaveReport() {
        if (reportArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay reporte para guardar. Ejecuta primero el análisis.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte");
        int sel = fc.showSaveDialog(this);
        if (sel == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(reportArea.getText());
                JOptionPane.showMessageDialog(this, "Reporte guardado: " + f.getAbsolutePath(), "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar reporte:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showGui() {
        SwingUtilities.invokeLater(() -> {
            ValidatorGUI gui = new ValidatorGUI();
            gui.setVisible(true);
        });
    }

    public static void main(String[] args) {
        showGui();
    }
}
