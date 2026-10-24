package interfaz;

import avilaos.Configuracion;
import avilaos.ConfiguracionJSON;
import avilaos.Metricas;
import avilaos.PoliticaPlanificacion;
import avilaos.Validaciones;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author andrea
 */
public class VistaConfiguracion extends JFrame {

    //quien quiera usar la configuracion que el usuario aplica implementa esto
    public interface ConfiguracionListener {
        void aplicar(Configuracion config);
    }

    private final ConfiguracionListener listener;
    //ultima configuracion valida, de aqui sale la lista de buffers y procesos que no se editan en esta ventana
    private Configuracion actual;

    private JTextField txtDuracion = new JTextField(8);
    private JTextField txtLatencia = new JTextField(8);
    private JSpinner spnComputadores = new JSpinner(new SpinnerNumberModel(2, 2, Metricas.MAX_COMPUTADORES, 1));

    //una fila por cada computador posible, solo se muestran las que se usan
    private JPanel[] filas = new JPanel[Metricas.MAX_COMPUTADORES];
    private JTextField[] txtRam = new JTextField[Metricas.MAX_COMPUTADORES];
    private JTextField[] txtQuantum = new JTextField[Metricas.MAX_COMPUTADORES];
    private JComboBox<String>[] cmbPolitica;

    @SuppressWarnings("unchecked")
    public VistaConfiguracion(ConfiguracionListener listener) {
        this.listener = listener;
        this.actual = Configuracion.porDefecto();
        this.cmbPolitica = new JComboBox[Metricas.MAX_COMPUTADORES];

        setTitle("Configuracion de la simulacion");
        //solo se cierra esta ventana, no todo el simulador
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel(new BorderLayout(10, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        //parte de arriba: lo que es para todo el sistema
        JPanel general = new JPanel(new GridLayout(3, 2, 10, 8));
        general.add(new JLabel("Duracion de un ciclo (ms)"));
        general.add(txtDuracion);
        general.add(new JLabel("Latencia de red (ciclos)"));
        general.add(txtLatencia);
        general.add(new JLabel("Cantidad de computadores"));
        general.add(spnComputadores);
        contenido.add(general, BorderLayout.NORTH);

        //parte del medio: una fila por computador con su ram, politica y quantum
        JPanel lista = new JPanel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createTitledBorder("Computadores"));
        String[] politicas = new String[PoliticaPlanificacion.values().length];
        for (int i = 0; i < politicas.length; i++) {
            politicas[i] = PoliticaPlanificacion.values()[i].name();
        }
        for (int i = 0; i < Metricas.MAX_COMPUTADORES; i++) {
            txtRam[i] = new JTextField(7);
            txtQuantum[i] = new JTextField(4);
            cmbPolitica[i] = new JComboBox<>(politicas);
            filas[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
            filas[i].add(new JLabel("Computador " + (i + 1)));
            filas[i].add(new JLabel("RAM"));
            filas[i].add(txtRam[i]);
            filas[i].add(new JLabel("Politica"));
            filas[i].add(cmbPolitica[i]);
            filas[i].add(new JLabel("Quantum"));
            filas[i].add(txtQuantum[i]);
            lista.add(filas[i]);
        }
        contenido.add(lista, BorderLayout.CENTER);

        //parte de abajo: los botones
        JButton btnCargar = new JButton("Cargar archivo");
        JButton btnGuardar = new JButton("Guardar archivo");
        JButton btnAplicar = new JButton("Aplicar");
        btnCargar.addActionListener(e -> cargar());
        btnGuardar.addActionListener(e -> guardar());
        btnAplicar.addActionListener(e -> aplicar());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCargar);
        botones.add(btnGuardar);
        botones.add(btnAplicar);
        contenido.add(botones, BorderLayout.SOUTH);

        //cuando cambia la cantidad de computadores se muestran u ocultan filas
        spnComputadores.addChangeListener(e -> mostrarFilas());

        setContentPane(contenido);
        mostrarEnFormulario(actual);
        setLocationRelativeTo(null);
    }

    //muestra solo las filas de los computadores que hay
    private void mostrarFilas() {
        int n = (Integer) spnComputadores.getValue();
        for (int i = 0; i < filas.length; i++) {
            filas[i].setVisible(i < n);
        }
        pack();
    }

    //pone en el formulario los datos de una configuracion
    private void mostrarEnFormulario(Configuracion c) {
        txtDuracion.setText(String.valueOf(c.duracionCicloMs));
        txtLatencia.setText(String.valueOf(c.latenciaRed));
        spnComputadores.setValue(c.computadores.length);
        for (int i = 0; i < c.computadores.length; i++) {
            txtRam[i].setText(String.valueOf(c.computadores[i].ram));
            txtQuantum[i].setText(String.valueOf(c.computadores[i].quantum));
            cmbPolitica[i].setSelectedItem(c.computadores[i].politica);
        }
        mostrarFilas();
    }

    //lee el formulario, valida cada campo y arma la configuracion
    //si algo esta mal lanza un error con el mensaje
    private Configuracion leerFormulario() {
        int duracion = Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 1, 60000);
        int latencia = Validaciones.leerEntero(txtLatencia.getText(), "Latencia de red", 0, 1000);
        int n = (Integer) spnComputadores.getValue();

        Configuracion.ConfigComputador[] computadores = new Configuracion.ConfigComputador[n];
        for (int i = 0; i < n; i++) {
            int ram = Validaciones.leerEntero(txtRam[i].getText(), "RAM del computador " + (i + 1), 1, 1000000);
            int quantum = Validaciones.leerEntero(txtQuantum[i].getText(), "Quantum del computador " + (i + 1), 1, 1000);
            String politica = (String) cmbPolitica[i].getSelectedItem();
            computadores[i] = new Configuracion.ConfigComputador(ram, politica, quantum);
        }

        Configuracion c = new Configuracion();
        c.duracionCicloMs = duracion;
        c.latenciaRed = latencia;
        c.computadores = computadores;
        //los buffers y procesos no se editan aqui, se conservan los que ya tenia
        c.buffers = actual.buffers;
        c.procesos = actual.procesos;
        //ultima revision, por ejemplo que ningun buffer quede en un computador que ya no existe
        c.validar();
        return c;
    }

    private void aplicar() {
        try {
            Configuracion c = leerFormulario();
            actual = c;
            if (listener != null) {
                listener.aplicar(c);
            }
            JOptionPane.showMessageDialog(this, "Configuracion aplicada");
        } catch (IllegalArgumentException e) {
            //dato invalido: aviso y el simulador sigue normal
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardar() {
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
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargar() {
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
    }
}