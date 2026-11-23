/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import Data_Structures.PCB;
import avilaos.DatosProceso;
import avilaos.InfoNodo;
import avilaos.ModoUsuario;
import avilaos.Permiso;
import avilaos.SistemaArchivos;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import avilaos.Validaciones;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author andrea
 */
//formulario para crear un proceso. La ventana no toca los archivos: solo crea el proceso que va a pedir la operacion
public class FormularioES extends JDialog {

    private static final String SIN_ES = "Sin E/S (solo CPU)";

    private SistemaOperativo so;
    private JTextField txtNombre = new JTextField();
    private JComboBox<String> cmbOperacion = new JComboBox<>(new String[]{
        SIN_ES, "CREAR", "LEER", "ACTUALIZAR", "ELIMINAR"});
    private JTextField txtRuta = new JTextField();
    private JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Archivo", "Directorio"});
    private JTextField txtTamano = new JTextField("1");
    private JComboBox<String> cmbPermiso = new JComboBox<>(new String[]{"publico", "privado"});
    private JTextField txtDueno = new JTextField();
    private JTextField txtNuevoNombre = new JTextField();
    private JTextField txtMemoria = new JTextField("16");
    private JTextField txtInstrucciones = new JTextField("2");
    private JTextField txtPrioridad = new JTextField("1");
    private JTextField txtDeadline = new JTextField("0");
    private JLabel lblMensaje = new JLabel(" ");
    private JLabel lblAyuda = new JLabel(" ");

    public FormularioES(JFrame padre, SistemaOperativo so) {
        super(padre, "Crear proceso", false);
        this.so = so;
        JPanel campos = new JPanel(new GridLayout(0, 2, 6, 5));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        agregar(campos, "Nombre del proceso", txtNombre);
        agregar(campos, "Operacion", cmbOperacion);
        agregar(campos, "Ruta del archivo o directorio", txtRuta);
        agregar(campos, "Que se crea", cmbTipo);
        agregar(campos, "Tamano en bloques", txtTamano);
        agregar(campos, "Permiso", cmbPermiso);
        agregar(campos, "Dueno (vacio = usuario actual)", txtDueno);
        agregar(campos, "Nombre nuevo (vacio = no renombrar)", txtNuevoNombre);
        agregar(campos, "Memoria (MB)", txtMemoria);
        agregar(campos, "Instrucciones", txtInstrucciones);
        agregar(campos, "Prioridad (1 a 10)", txtPrioridad);
        agregar(campos, "Deadline en ciclos (0 = sin deadline)", txtDeadline);

        JPanel abajo = new JPanel(new GridLayout(3, 1, 0, 4));
        abajo.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton btnCrear = new JButton("Crear proceso");
        btnCrear.addActionListener(e -> crear());
        abajo.add(lblAyuda);
        abajo.add(lblMensaje);
        abajo.add(btnCrear);

        setLayout(new BorderLayout());
        add(campos, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
        cmbOperacion.addActionListener(e -> actualizarCampos());
        cmbTipo.addActionListener(e -> actualizarCampos());
        getRootPane().setDefaultButton(btnCrear);
        setSize(560, 520);
        setLocationRelativeTo(padre);
        nombrePorDefecto();
        actualizarCampos();
    }

    private void agregar(JPanel panel, String texto, java.awt.Component campo) {
        panel.add(new JLabel(texto));
        panel.add(campo);
    }

    private void nombrePorDefecto() {
        txtNombre.setText("P" + so.getSiguienteId());
    }

    private TipoOperacionES operacionElegida() {
        String texto = (String) cmbOperacion.getSelectedItem();
        return SIN_ES.equals(texto) ? null : TipoOperacionES.valueOf(texto);
    }

    //solo quedan activos los campos que usa la operacion escogida
    private void actualizarCampos() {
        TipoOperacionES op = operacionElegida();
        boolean crea = op == TipoOperacionES.CREAR;
        boolean creaArchivo = crea && cmbTipo.getSelectedIndex() == 0;
        boolean admin = so.getModoUsuario() == ModoUsuario.ADMINISTRADOR;
        txtRuta.setEnabled(op != null);
        cmbTipo.setEnabled(crea);
        txtTamano.setEnabled(creaArchivo);
        cmbPermiso.setEnabled(crea);
        txtDueno.setEnabled(crea && admin);
        txtNuevoNombre.setEnabled(op == TipoOperacionES.ACTUALIZAR);
        lblAyuda.setText("Modo activo: " + so.getModoUsuario() + " (" + so.getUsuario() + ")");
    }

    //la ventana principal lo llama cuando aprietan un boton del arbol, para dejar el formulario medio lleno
    public void preparar(TipoOperacionES op, InfoNodo sobre, boolean directorio) {
        lblMensaje.setText(" ");
        cmbOperacion.setSelectedItem(op == null ? SIN_ES : op.name());
        if (op == TipoOperacionES.CREAR) {
            //lo nuevo va dentro del directorio seleccionado, o al lado del archivo seleccionado
            String base = "/";
            if (sobre != null) {
                base = sobre.esDirectorio() ? sobre.ruta : SistemaArchivos.rutaPadre(sobre.ruta);
            }
            cmbTipo.setSelectedIndex(directorio ? 1 : 0);
            txtRuta.setText(SistemaArchivos.unir(base, directorio ? "nuevo" : "nuevo.txt"));
        } else if (op != null && sobre != null) {
            txtRuta.setText(sobre.ruta);
        }
        txtNuevoNombre.setText("");
        nombrePorDefecto();
        actualizarCampos();
    }

    private void crear() {
        actualizarCampos();
        try {
            DatosProceso d = new DatosProceso();
            d.nombre = Validaciones.leerTexto(txtNombre.getText(), "Nombre", 30);
            d.memoria = Validaciones.leerDecimal(txtMemoria.getText(), "Memoria", 1, 1000000);
            d.instrucciones = Validaciones.leerEntero(txtInstrucciones.getText(), "Instrucciones", 1, 100000);
            d.prioridad = Validaciones.leerEntero(txtPrioridad.getText(), "Prioridad", 1, 10);
            d.deadline = Validaciones.leerEntero(txtDeadline.getText(), "Deadline", 0, 100000);
            d.operacion = operacionElegida();
            if (d.operacion != null) {
                d.ruta = Validaciones.leerTexto(txtRuta.getText(), "Ruta", 200);
                if (d.operacion == TipoOperacionES.CREAR) {
                    d.esDirectorio = cmbTipo.getSelectedIndex() == 1;
                    if (!d.esDirectorio) {
                        d.tamano = Validaciones.leerEntero(txtTamano.getText(), "Tamano en bloques", 1, 500);
                    }
                    d.permiso = cmbPermiso.getSelectedIndex() == 0 ? Permiso.PUBLICO : Permiso.PRIVADO;
                    d.dueno = txtDueno.getText().trim();
                    if (!d.dueno.isEmpty()) {
                        d.dueno = Validaciones.leerTexto(d.dueno, "Dueno", 30);
                    }
                }
                if (d.operacion == TipoOperacionES.ACTUALIZAR && !txtNuevoNombre.getText().trim().isEmpty()) {
                    d.nuevoNombre = Validaciones.leerTexto(txtNuevoNombre.getText(), "Nombre nuevo", 30);
                }
            }
            PCB p = so.crearProceso(d);
            lblMensaje.setForeground(new Color(20, 120, 60));
            lblMensaje.setText("Proceso " + p.getname() + " creado, ya esta en la cola de nuevos");
            nombrePorDefecto();
        } catch (IllegalArgumentException e) {
            //la entrada mala o la operacion rechazada se explican aqui y la simulacion sigue corriendo
            lblMensaje.setForeground(new Color(190, 30, 30));
            lblMensaje.setText(e.getMessage());
            lblMensaje.setToolTipText(e.getMessage());
        }
    }
}
