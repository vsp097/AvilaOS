/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.SistemaOperativo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author vyckh
 */
//la primera ventana: no deja arrancar hasta que esten cargados los dos archivos json
public class inicio extends JFrame {

    private JTextField txtArchivos = new JTextField();
    private JTextField txtProcesos = new JTextField();
    private JLabel lblArchivos = new JLabel(" ");
    private JLabel lblProcesos = new JLabel(" ");
    private JLabel lblError = new JLabel(" ");
    private JButton btnIniciar = new JButton("Abrir el simulador");
    //los dos archivos ya leidos y validados, null mientras no se hayan cargado
    private ConfigArchivos configArchivos;
    private ConfigProcesos configProcesos;

    public inicio() {
        super("AvilaOS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel titulo = new JLabel("AvilaOS - carga de archivos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));

        JPanel centro = new JPanel(new GridLayout(0, 1, 0, 4));
        centro.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        centro.add(new JLabel("1. Sistema de archivos (disco, directorios y archivos)"));
        centro.add(fila(txtArchivos, true));
        centro.add(lblArchivos);
        centro.add(new JLabel("2. Procesos y configuracion (RAM, politicas, area de intercambio)"));
        centro.add(fila(txtProcesos, false));
        centro.add(lblProcesos);
        centro.add(lblError);

        JPanel abajo = new JPanel();
        abajo.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(e -> abrir());
        abajo.add(btnIniciar);

        setLayout(new BorderLayout());
        add(titulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
        lblError.setForeground(new Color(190, 30, 30));
        setSize(720, 330);
        setLocationRelativeTo(null);

        //si estan los archivos de ejemplo del proyecto se dejan escritos, pero igual hay que cargarlos
        sugerir(txtArchivos, "datos/sistema_archivos.json");
        sugerir(txtProcesos, "datos/procesos_fifo.json");
    }

    private void sugerir(JTextField campo, String ruta) {
        if (new File(ruta).isFile()) {
            campo.setText(ruta);
        }
    }

    private JPanel fila(final JTextField campo, final boolean esArchivos) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JPanel botones = new JPanel(new GridLayout(1, 2, 6, 0));
        JButton buscar = new JButton("Buscar...");
        JButton cargar = new JButton("Cargar");
        buscar.addActionListener(e -> {
            JFileChooser selector = new JFileChooser(new File(campo.getText().isEmpty() ? "." : campo.getText()).getAbsoluteFile().getParentFile());
            selector.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
            if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                campo.setText(selector.getSelectedFile().getPath());
                cargar(esArchivos);
            }
        });
        cargar.addActionListener(e -> cargar(esArchivos));
        botones.add(buscar);
        botones.add(cargar);
        p.add(campo, BorderLayout.CENTER);
        p.add(botones, BorderLayout.EAST);
        return p;
    }

    //lee y valida uno de los dos archivos; si esta malo dice por que y no deja seguir
    private void cargar(boolean esArchivos) {
        JLabel estado = esArchivos ? lblArchivos : lblProcesos;
        String ruta = (esArchivos ? txtArchivos : txtProcesos).getText().trim();
        lblError.setText(" ");
        try {
            if (ruta.isEmpty()) {
                throw new IllegalArgumentException("Escribe o busca la ruta del archivo");
            }
            if (esArchivos) {
                configArchivos = null;
                configArchivos = ArchivosJSON.cargarArchivos(ruta);
                estado.setText("Cargado: disco de " + configArchivos.diskConfig.totalBlocks + " bloques, cabezal en "
                        + configArchivos.diskConfig.initialHead + ", " + configArchivos.fileSystem.length + " elementos");
            } else {
                configProcesos = null;
                configProcesos = ArchivosJSON.cargarProcesos(ruta);
                estado.setText("Cargado: " + configProcesos.processes.length + " procesos, RAM "
                        + configProcesos.computer.ramCapacity + ", CPU " + configProcesos.computer.policy + ", disco "
                        + configProcesos.globalConfig.initialDiskPolicy + ", intercambio "
                        + configProcesos.globalConfig.swapSizeBlocks + " bloques");
            }
            estado.setForeground(new Color(20, 120, 60));
        } catch (IOException e) {
            estado.setForeground(new Color(190, 30, 30));
            estado.setText("No se pudo leer el archivo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            estado.setForeground(new Color(190, 30, 30));
            estado.setText(e.getMessage());
        }
        estado.setToolTipText(estado.getText());
        btnIniciar.setEnabled(configArchivos != null && configProcesos != null);
    }

    private void abrir() {
        try {
            //aqui se arma todo el sistema; si los dos archivos no cuadran entre si, avisa
            SistemaOperativo so = new SistemaOperativo(configArchivos, configProcesos);
            main ventana = new main(so);
            ventana.setVisible(true);
            dispose();
        } catch (IllegalArgumentException e) {
            lblError.setText(e.getMessage());
            lblError.setToolTipText(e.getMessage());
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println("No se pudo poner el estilo Nimbus");
        }
        java.awt.EventQueue.invokeLater(() -> new inicio().setVisible(true));
    }
}
