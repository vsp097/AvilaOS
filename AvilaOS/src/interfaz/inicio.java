/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.SistemaOperativo;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
//--- fin del bloque a: imports ---

/**
 *
 * @author diego
 */
//la primera ventana: no deja arrancar hasta que esten cargados los dos archivos json
public class inicio extends javax.swing.JFrame {

    //--- bloque a: campos ---
    //los dos archivos ya leidos y validados, null mientras no se hayan cargado
    private ConfigArchivos configArchivos;
    private ConfigProcesos configProcesos;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form inicio
     */
    public inicio() {
        initComponents();
        //--- bloque b ---
        setSize(720, 330);
        setLocationRelativeTo(null);
        //si estan los archivos de ejemplo del proyecto se dejan escritos, pero igual hay que cargarlos
        sugerir(txtArchivos, "datos/sistema_archivos.json");
        sugerir(txtProcesos, "datos/procesos_fifo.json");
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    private void sugerir(JTextField campo, String ruta) {
        if (new File(ruta).isFile()) {
            campo.setText(ruta);
        }
    }

    //abre el selector de archivos empezando en la carpeta de lo que ya esta escrito
    private void buscar(JTextField campo, boolean esArchivos) {
        File actual = new File(campo.getText().isEmpty() ? "." : campo.getText()).getAbsoluteFile();
        JFileChooser selector = new JFileChooser(actual.getParentFile());
        selector.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campo.setText(selector.getSelectedFile().getPath());
            cargar(esArchivos);
        }
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

    private void btnBuscarArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarArchivosActionPerformed
        buscar(txtArchivos, true);
    }//GEN-LAST:event_btnBuscarArchivosActionPerformed

    private void btnCargarArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarArchivosActionPerformed
        cargar(true);
    }//GEN-LAST:event_btnCargarArchivosActionPerformed

    private void btnBuscarProcesosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProcesosActionPerformed
        buscar(txtProcesos, false);
    }//GEN-LAST:event_btnBuscarProcesosActionPerformed

    private void btnCargarProcesosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarProcesosActionPerformed
        cargar(false);
    }//GEN-LAST:event_btnCargarProcesosActionPerformed

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
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
    }//GEN-LAST:event_btnIniciarActionPerformed
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        pnlCentro = new javax.swing.JPanel();
        lblPaso1 = new javax.swing.JLabel();
        pnlFilaArchivos = new javax.swing.JPanel();
        txtArchivos = new javax.swing.JTextField();
        pnlBotonesArchivos = new javax.swing.JPanel();
        btnBuscarArchivos = new javax.swing.JButton();
        btnCargarArchivos = new javax.swing.JButton();
        lblArchivos = new javax.swing.JLabel();
        lblPaso2 = new javax.swing.JLabel();
        pnlFilaProcesos = new javax.swing.JPanel();
        txtProcesos = new javax.swing.JTextField();
        pnlBotonesProcesos = new javax.swing.JPanel();
        btnBuscarProcesos = new javax.swing.JButton();
        btnCargarProcesos = new javax.swing.JButton();
        lblProcesos = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        pnlAbajo = new javax.swing.JPanel();
        btnIniciar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AvilaOS");

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(lblTitulo.getFont().getStyle() | java.awt.Font.BOLD, lblTitulo.getFont().getSize()+6));
        lblTitulo.setText("AvilaOS - carga de archivos");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 4, 16));
        getContentPane().add(lblTitulo, java.awt.BorderLayout.PAGE_START);

        pnlCentro.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        pnlCentro.setLayout(new java.awt.GridLayout(7, 1, 0, 4));

        lblPaso1.setText("1. Sistema de archivos (disco, directorios y archivos)");
        pnlCentro.add(lblPaso1);

        pnlFilaArchivos.setLayout(new java.awt.BorderLayout(6, 0));
        pnlFilaArchivos.add(txtArchivos, java.awt.BorderLayout.CENTER);

        pnlBotonesArchivos.setLayout(new java.awt.GridLayout(1, 2, 6, 0));

        btnBuscarArchivos.setText("Buscar...");
        btnBuscarArchivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarArchivosActionPerformed(evt);
            }
        });
        pnlBotonesArchivos.add(btnBuscarArchivos);

        btnCargarArchivos.setText("Cargar");
        btnCargarArchivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarArchivosActionPerformed(evt);
            }
        });
        pnlBotonesArchivos.add(btnCargarArchivos);

        pnlFilaArchivos.add(pnlBotonesArchivos, java.awt.BorderLayout.LINE_END);

        pnlCentro.add(pnlFilaArchivos);

        lblArchivos.setText(" ");
        pnlCentro.add(lblArchivos);

        lblPaso2.setText("2. Procesos y configuracion (RAM, politicas, area de intercambio)");
        pnlCentro.add(lblPaso2);

        pnlFilaProcesos.setLayout(new java.awt.BorderLayout(6, 0));
        pnlFilaProcesos.add(txtProcesos, java.awt.BorderLayout.CENTER);

        pnlBotonesProcesos.setLayout(new java.awt.GridLayout(1, 2, 6, 0));

        btnBuscarProcesos.setText("Buscar...");
        btnBuscarProcesos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProcesosActionPerformed(evt);
            }
        });
        pnlBotonesProcesos.add(btnBuscarProcesos);

        btnCargarProcesos.setText("Cargar");
        btnCargarProcesos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarProcesosActionPerformed(evt);
            }
        });
        pnlBotonesProcesos.add(btnCargarProcesos);

        pnlFilaProcesos.add(pnlBotonesProcesos, java.awt.BorderLayout.LINE_END);

        pnlCentro.add(pnlFilaProcesos);

        lblProcesos.setText(" ");
        pnlCentro.add(lblProcesos);

        lblError.setForeground(new java.awt.Color(190, 30, 30));
        lblError.setText(" ");
        pnlCentro.add(lblError);

        getContentPane().add(pnlCentro, java.awt.BorderLayout.CENTER);

        pnlAbajo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 12, 16));

        btnIniciar.setText("Abrir el simulador");
        btnIniciar.setEnabled(false);
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });
        pnlAbajo.add(btnIniciar);

        getContentPane().add(pnlAbajo, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarArchivos;
    private javax.swing.JButton btnBuscarProcesos;
    private javax.swing.JButton btnCargarArchivos;
    private javax.swing.JButton btnCargarProcesos;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JLabel lblArchivos;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblPaso1;
    private javax.swing.JLabel lblPaso2;
    private javax.swing.JLabel lblProcesos;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlAbajo;
    private javax.swing.JPanel pnlBotonesArchivos;
    private javax.swing.JPanel pnlBotonesProcesos;
    private javax.swing.JPanel pnlCentro;
    private javax.swing.JPanel pnlFilaArchivos;
    private javax.swing.JPanel pnlFilaProcesos;
    private javax.swing.JTextField txtArchivos;
    private javax.swing.JTextField txtProcesos;
    // End of variables declaration//GEN-END:variables
    //--- fin del diseno ---
}
