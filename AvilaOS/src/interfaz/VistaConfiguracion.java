/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

import avilaos.Configuracion;
import avilaos.ConfiguracionJSON;
import avilaos.Metricas;
import avilaos.PoliticaPlanificacion;
import avilaos.Validaciones;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author vyckh
 */
public class VistaConfiguracion extends javax.swing.JPanel {

    /**
     * Creates new form VistaConfiguracion
     */
       //quien quiera usar la configuracion que se aplica implementa esto
    public interface ConfiguracionListener {
        void aplicar(Configuracion config);
    }

    private ConfiguracionListener listener;
    //ultima configuracion valida, de aqui salen los buffers y procesos que no se editan en esta ventana
    private Configuracion actual;
    //datos de cada computador, el que se esta editando se guarda al cambiar de computador
    private Configuracion.ConfigComputador[] datos = new Configuracion.ConfigComputador[Metricas.MAX_COMPUTADORES];
    //cual computador se esta editando (0 es el primero)
    private int indiceActual = 0;
    //true mientras cambio los campos por codigo, para que los eventos no se disparen solos
    private boolean cargando = false;

    //constructor vacio por si otra ventana ya lo usa
    public VistaConfiguracion() {
        this(null);
    }

    //despues de aplicar una vez el boton se apaga, para no reiniciar la RAM con procesos corriendo
    public void desactivarAplicar() {
        btnAplicar.setEnabled(false);
    }

    public VistaConfiguracion(ConfiguracionListener listener) {
        initComponents();
        this.listener = listener;
        spnComputadores.setModel(new SpinnerNumberModel(2, 2, Metricas.MAX_COMPUTADORES, 1));
        String[] politicas = new String[PoliticaPlanificacion.values().length];
        for (int i = 0; i < politicas.length; i++) {
            politicas[i] = PoliticaPlanificacion.values()[i].name();
        }
        cmbPolitica.setModel(new DefaultComboBoxModel<>(politicas));
        actual = Configuracion.porDefecto();
        mostrarEnFormulario(actual);
    }

    //llena el desplegable de computadores con la cantidad que hay
    private void llenarComputadores(int n) {
        String[] nombres = new String[n];
        for (int i = 0; i < n; i++) {
            nombres[i] = "Computador " + (i + 1);
        }
        cmbComputadorEditar.setModel(new DefaultComboBoxModel<>(nombres));
    }

    //muestra en los campos los datos de un computador
    private void mostrarFila(int i) {
        txtRam.setText(String.valueOf(datos[i].ram));
        txtQuantum.setText(String.valueOf(datos[i].quantum));
        cmbPolitica.setSelectedItem(datos[i].politica);
    }

    //valida los campos del computador que se esta editando y los guarda, si algo esta mal lanza error
    private void guardarFilaActual() {
        int ram = Validaciones.leerEntero(txtRam.getText(), "RAM del computador " + (indiceActual + 1), 1, 1000000);
        int quantum = Validaciones.leerEntero(txtQuantum.getText(), "Quantum del computador " + (indiceActual + 1), 1, 1000);
        String politica = (String) cmbPolitica.getSelectedItem();
        datos[indiceActual] = new Configuracion.ConfigComputador(ram, politica, quantum);
    }

    //pone en la ventana los datos de una configuracion
    private void mostrarEnFormulario(Configuracion c) {
        cargando = true;
        txtDuracion.setText(String.valueOf(c.duracionCicloMs));
        txtLatencia.setText(String.valueOf(c.latenciaRed));
        for (int i = 0; i < datos.length; i++) {
            if (i < c.computadores.length) {
                datos[i] = c.computadores[i];
            } else {
                datos[i] = new Configuracion.ConfigComputador(1024, "FCFS", 4);
            }
        }
        spnComputadores.setValue(c.computadores.length);
        llenarComputadores(c.computadores.length);
        indiceActual = 0;
        cmbComputadorEditar.setSelectedIndex(0);
        mostrarFila(0);
        cargando = false;
    }

    //lee toda la ventana, valida y arma la configuracion, si algo esta mal lanza error
    private Configuracion leerFormulario() {
        int duracion = Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 1, 60000);
        int latencia = Validaciones.leerEntero(txtLatencia.getText(), "Latencia de red", 0, 1000);
        guardarFilaActual();
        int n = (Integer) spnComputadores.getValue();
        Configuracion.ConfigComputador[] computadores = new Configuracion.ConfigComputador[n];
        for (int i = 0; i < n; i++) {
            computadores[i] = datos[i];
        }
        Configuracion c = new Configuracion();
        c.duracionCicloMs = duracion;
        c.latenciaRed = latencia;
        c.computadores = computadores;
        //los buffers y procesos no se editan aqui, se conservan los que ya tenia
        c.buffers = actual.buffers;
        c.procesos = actual.procesos;
        //ultima revision, por ejemplo que ningun buffer quede en un computador que no existe
        c.validar();
        return c;
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Dato invalido", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ayuda = new javax.swing.JLabel();
        campos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtLatencia = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        spnComputadores = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        cmbComputadorEditar = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        txtRam = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbPolitica = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtQuantum = new javax.swing.JTextField();
        acciones = new javax.swing.JPanel();
        botones = new javax.swing.JPanel();
        btnCargar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnAplicar = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        ayuda.setText("Cambia los parámetros, guarda o carga un archivo JSON, y presiona Aplicar para usarlos.");
        ayuda.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(ayuda, java.awt.BorderLayout.NORTH);

        campos.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Duración de un ciclo (ms)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(txtDuracion, gridBagConstraints);

        jLabel2.setText("Latencia de red (ciclos)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(txtLatencia, gridBagConstraints);

        jLabel3.setText("Cantidad de computadores");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel3, gridBagConstraints);
        spnComputadores.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnComputadoresStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(spnComputadores, gridBagConstraints);

        jLabel4.setText("Computador a editar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel4, gridBagConstraints);
        cmbComputadorEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbComputadorEditarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(cmbComputadorEditar, gridBagConstraints);

        jLabel5.setText("RAM (MB)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(txtRam, gridBagConstraints);

        jLabel6.setText("Política inicial");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(cmbPolitica, gridBagConstraints);

        jLabel7.setText("Quantum");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 10);
        campos.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        campos.add(txtQuantum, gridBagConstraints);

        add(campos, java.awt.BorderLayout.CENTER);

        acciones.setLayout(new java.awt.BorderLayout());

        botones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCargar.setText("Cargar archivo");
        btnCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarActionPerformed(evt);
            }
        });
        botones.add(btnCargar);

        btnGuardar.setText("Guardar archivo");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        botones.add(btnGuardar);

        btnAplicar.setText("Aplicar");
        btnAplicar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAplicarActionPerformed(evt);
            }
        });
        botones.add(btnAplicar);

        acciones.add(botones, java.awt.BorderLayout.EAST);
        add(acciones, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbComputadorEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbComputadorEditarActionPerformed
                if (cargando) {
            return;
        }
        int nuevo = cmbComputadorEditar.getSelectedIndex();
        if (nuevo < 0 || nuevo == indiceActual) {
            return;
        }
        try {
            guardarFilaActual();
            indiceActual = nuevo;
            mostrarFila(nuevo);
        } catch (IllegalArgumentException e) {
            //dato invalido: aviso y me quedo en el mismo computador
            avisar(e.getMessage());
            cargando = true;
            cmbComputadorEditar.setSelectedIndex(indiceActual);
            cargando = false;
        }
    }//GEN-LAST:event_cmbComputadorEditarActionPerformed

    private void spnComputadoresStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnComputadoresStateChanged
                if (cargando) {
            return;
        }
        int n = (Integer) spnComputadores.getValue();
        try {
            guardarFilaActual();
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
            cargando = true;
            spnComputadores.setValue(cmbComputadorEditar.getItemCount());
            cargando = false;
            return;
        }
        cargando = true;
        llenarComputadores(n);
        if (indiceActual >= n) {
            indiceActual = 0;
        }
        cmbComputadorEditar.setSelectedIndex(indiceActual);
        mostrarFila(indiceActual);
        cargando = false;
    }//GEN-LAST:event_spnComputadoresStateChanged

    private void btnAplicarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAplicarActionPerformed
                try {
            Configuracion c = leerFormulario();
            actual = c;
            if (listener != null) {
                listener.aplicar(c);
            }
            JOptionPane.showMessageDialog(this, "Configuracion aplicada");
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }//GEN-LAST:event_btnAplicarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
                try {
            Configuracion c = leerFormulario();
            JFileChooser selector = new JFileChooser();
            selector.setSelectedFile(new File("configuracion.json"));
            if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String ruta = selector.getSelectedFile().getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".json")) {
                ruta = ruta + ".json";
            }
            ConfiguracionJSON.guardar(c, ruta);
            actual = c;
            JOptionPane.showMessageDialog(this, "Configuracion guardada");
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
                JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Configuracion c = ConfiguracionJSON.cargar(selector.getSelectedFile().getAbsolutePath());
            actual = c;
            mostrarEnFormulario(c);
        } catch (IllegalArgumentException e) {
            //el archivo tenia datos fuera de rango o no era un json valido
            JOptionPane.showMessageDialog(this, e.getMessage(), "Archivo invalido", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el archivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCargarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel acciones;
    private javax.swing.JLabel ayuda;
    private javax.swing.JPanel botones;
    private javax.swing.JButton btnAplicar;
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JPanel campos;
    private javax.swing.JComboBox<String> cmbComputadorEditar;
    private javax.swing.JComboBox<String> cmbPolitica;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSpinner spnComputadores;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JTextField txtLatencia;
    private javax.swing.JTextField txtQuantum;
    private javax.swing.JTextField txtRam;
    // End of variables declaration//GEN-END:variables
}
