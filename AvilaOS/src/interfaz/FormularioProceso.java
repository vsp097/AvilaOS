package interfaz;

import Data_Structures.PCB;
import avilaos.SolicitudProceso;
import avilaos.Validaciones;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author andrea
 */
public class FormularioProceso extends JFrame {

    //quien quiera recibir los procesos que se crean implementa esto
    public interface ProcesoCreadoListener {
        void procesoCreado(SolicitudProceso solicitud);
    }

    //contador para darle un id distinto a cada proceso
    //(si el equipo hace un generador de ids global, se cambia aqui)
    private static int contadorId = 1;

    private final ProcesoCreadoListener listener;

    private JTextField txtNombre = new JTextField();
    private JTextField txtInstrucciones = new JTextField();
    private JTextField txtMemoria = new JTextField();
    private JTextField txtPrioridad = new JTextField();
    private JTextField txtDeadline = new JTextField();
    private JTextField txtCiclos = new JTextField();
    private JTextField txtCadaCiclos = new JTextField();
    private JTextField txtElementos = new JTextField();
    private JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"CPU Bound", "I/O Bound", "Productor", "Consumidor"});
    private JComboBox<String> cmbComputador;
    private JComboBox<String> cmbBuffer;

    //filas que se muestran o se esconden segun el tipo de proceso
    private JPanel filaCiclos;
    private JPanel filaBuffer;
    private JPanel filaCadaCiclos;
    private JPanel filaElementos;

    //recibe cuantos computadores hay y los nombres de los buffers que existen
    public FormularioProceso(int cantidadComputadores, String[] nombresBuffers, ProcesoCreadoListener listener) {
        this.listener = listener;
        if (nombresBuffers == null) {
            nombresBuffers = new String[0];
        }
        if (cantidadComputadores < 1) {
            cantidadComputadores = 1;
        }

        String[] computadores = new String[cantidadComputadores];
        for (int i = 0; i < cantidadComputadores; i++) {
            computadores[i] = "Computador " + (i + 1);
        }
        cmbComputador = new JComboBox<>(computadores);
        cmbBuffer = new JComboBox<>(nombresBuffers);

        setTitle("Crear proceso");
        //solo se cierra esta ventana, no todo el simulador
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        filaCiclos = fila("Ciclos para terminar", txtCiclos);
        filaBuffer = fila("Buffer", cmbBuffer);
        filaCadaCiclos = fila("Cada cuantos ciclos", txtCadaCiclos);
        filaElementos = fila("Elementos que necesita", txtElementos);

        contenido.add(fila("Nombre", txtNombre));
        contenido.add(fila("Cantidad de instrucciones", txtInstrucciones));
        contenido.add(fila("Memoria que requiere", txtMemoria));
        contenido.add(fila("Prioridad (1 es la mayor)", txtPrioridad));
        contenido.add(fila("Deadline (0 = sin deadline)", txtDeadline));
        contenido.add(fila("Tipo de proceso", cmbTipo));
        contenido.add(fila("Computador", cmbComputador));
        contenido.add(filaCiclos);
        contenido.add(filaBuffer);
        contenido.add(filaCadaCiclos);
        contenido.add(filaElementos);

        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregar());
        JPanel filaBoton = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        filaBoton.add(btnAgregar);
        contenido.add(filaBoton);

        cmbTipo.addActionListener(e -> actualizarCampos());

        setContentPane(contenido);
        actualizarCampos();
        setLocationRelativeTo(null);
    }

    //arma una fila con el texto a la izquierda y el campo a la derecha
    private JPanel fila(String texto, JComponent campo) {
        JPanel p = new JPanel(new java.awt.BorderLayout(10, 0));
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setPreferredSize(new java.awt.Dimension(190, 25));
        p.add(etiqueta, java.awt.BorderLayout.WEST);
        p.add(campo, java.awt.BorderLayout.CENTER);
        p.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 32));
        return p;
    }

    private boolean esProductorOConsumidor() {
        String tipo = (String) cmbTipo.getSelectedItem();
        return tipo.equals("Productor") || tipo.equals("Consumidor");
    }

    //muestra solo los campos que le tocan al tipo de proceso escogido
    private void actualizarCampos() {
        boolean prodCons = esProductorOConsumidor();
        filaCiclos.setVisible(!prodCons);
        filaBuffer.setVisible(prodCons);
        filaCadaCiclos.setVisible(prodCons);
        filaElementos.setVisible(prodCons);
        pack();
    }

    //valida todo, crea el pcb y se lo manda al listener
    private void agregar() {
        try {
            String nombre = Validaciones.leerTexto(txtNombre.getText(), "Nombre", 30);
            int instrucciones = Validaciones.leerEntero(txtInstrucciones.getText(), "Cantidad de instrucciones", 1, 100000);
            float memoria = Validaciones.leerDecimal(txtMemoria.getText(), "Memoria", 1, 100000);
            int prioridad = Validaciones.leerEntero(txtPrioridad.getText(), "Prioridad", 1, 10);
            int deadline = Validaciones.leerEntero(txtDeadline.getText(), "Deadline", 0, 100000);
            String tipo = (String) cmbTipo.getSelectedItem();
            int computador = cmbComputador.getSelectedIndex() + 1;

            int ciclos = 0;
            int buffer = -1;
            int cadaCiclos = 0;
            int elementos = 0;

            if (esProductorOConsumidor()) {
                if (cmbBuffer.getItemCount() == 0) {
                    throw new IllegalArgumentException("Primero hay que crear un buffer");
                }
                buffer = cmbBuffer.getSelectedIndex();
                cadaCiclos = Validaciones.leerEntero(txtCadaCiclos.getText(), "Cada cuantos ciclos", 1, 1000);
                elementos = Validaciones.leerEntero(txtElementos.getText(), "Elementos que necesita", 1, 100000);
            } else {
                ciclos = Validaciones.leerEntero(txtCiclos.getText(), "Ciclos para terminar", 1, 100000);
            }

            //el pcb de diego solo recibe algunos datos en el constructor, el resto va con set
            PCB pcb = new PCB(contadorId, nombre, 0, "Nuevo", tipo, memoria);
            contadorId++;
            pcb.setPrioridad(prioridad);
            pcb.setdeadline(deadline);
            //guardo las instrucciones como tiempo restante de ejecucion
            pcb.settime(instrucciones);

            SolicitudProceso solicitud = new SolicitudProceso(pcb, computador, buffer, ciclos, cadaCiclos, elementos);
            if (listener != null) {
                listener.procesoCreado(solicitud);
            }
            JOptionPane.showMessageDialog(this, "Proceso " + nombre + " creado");
            limpiar();
        } catch (IllegalArgumentException e) {
            //dato invalido: aviso y el simulador sigue normal
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtInstrucciones.setText("");
        txtMemoria.setText("");
        txtPrioridad.setText("");
        txtDeadline.setText("");
        txtCiclos.setText("");
        txtCadaCiclos.setText("");
        txtElementos.setText("");
    }
}