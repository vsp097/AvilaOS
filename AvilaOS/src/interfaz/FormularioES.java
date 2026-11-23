/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import Data_Structures.PCB;
import avilaos.DatosProceso;
import avilaos.InfoNodo;
import avilaos.ModoUsuario;
import avilaos.Permiso;
import avilaos.SistemaArchivos;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import avilaos.Validaciones;
import java.awt.Color;
//--- fin del bloque a: imports ---

/**
 *
 * @author andrea
 */
//formulario para crear un proceso. La ventana no toca los archivos: solo crea el proceso que va a pedir la operacion
public class FormularioES extends javax.swing.JDialog {

    //--- bloque a: campos ---
    private static final String SIN_ES = "Sin E/S (solo CPU)";
    private SistemaOperativo so;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form FormularioES
     */
    public FormularioES(java.awt.Frame parent, SistemaOperativo so) {
        super(parent, false);
        initComponents();
        //--- bloque b ---
        this.so = so;
        //las opciones de los selectores se llenan aqui, en el disenador quedan vacios
        cmbOperacion.addItem(SIN_ES);
        cmbOperacion.addItem("CREAR");
        cmbOperacion.addItem("LEER");
        cmbOperacion.addItem("ACTUALIZAR");
        cmbOperacion.addItem("ELIMINAR");
        cmbTipo.addItem("Archivo");
        cmbTipo.addItem("Directorio");
        cmbPermiso.addItem("publico");
        cmbPermiso.addItem("privado");
        getRootPane().setDefaultButton(btnCrear);
        setSize(560, 520);
        setLocationRelativeTo(parent);
        nombrePorDefecto();
        actualizarCampos();
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    private void nombrePorDefecto() {
        txtNombre.setText("P" + so.getSiguienteId());
    }

    private TipoOperacionES operacionElegida() {
        String texto = (String) cmbOperacion.getSelectedItem();
        return texto == null || SIN_ES.equals(texto) ? null : TipoOperacionES.valueOf(texto);
    }

    //solo quedan activos los campos que usa la operacion escogida
    private void actualizarCampos() {
        if (so == null) {
            //todavia se esta armando la ventana
            return;
        }
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

    private void cmbOperacionActionPerformed(java.awt.event.ActionEvent evt) {
        actualizarCampos();
    }

    private void cmbTipoActionPerformed(java.awt.event.ActionEvent evt) {
        actualizarCampos();
    }

    private void btnCrearActionPerformed(java.awt.event.ActionEvent evt) {
        crear();
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        pnlCampos = new javax.swing.JPanel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblOperacion = new javax.swing.JLabel();
        cmbOperacion = new javax.swing.JComboBox<>();
        lblRuta = new javax.swing.JLabel();
        txtRuta = new javax.swing.JTextField();
        lblTipo = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox<>();
        lblTamano = new javax.swing.JLabel();
        txtTamano = new javax.swing.JTextField();
        lblPermiso = new javax.swing.JLabel();
        cmbPermiso = new javax.swing.JComboBox<>();
        lblDueno = new javax.swing.JLabel();
        txtDueno = new javax.swing.JTextField();
        lblNuevoNombre = new javax.swing.JLabel();
        txtNuevoNombre = new javax.swing.JTextField();
        lblMemoria = new javax.swing.JLabel();
        txtMemoria = new javax.swing.JTextField();
        lblInstrucciones = new javax.swing.JLabel();
        txtInstrucciones = new javax.swing.JTextField();
        lblPrioridad = new javax.swing.JLabel();
        txtPrioridad = new javax.swing.JTextField();
        lblDeadline = new javax.swing.JLabel();
        txtDeadline = new javax.swing.JTextField();
        pnlAbajo = new javax.swing.JPanel();
        lblAyuda = new javax.swing.JLabel();
        lblMensaje = new javax.swing.JLabel();
        btnCrear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Crear proceso");

        pnlCampos.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 6, 10));
        pnlCampos.setLayout(new java.awt.GridLayout(12, 2, 6, 5));

        lblNombre.setText("Nombre del proceso");
        pnlCampos.add(lblNombre);

        pnlCampos.add(txtNombre);

        lblOperacion.setText("Operacion");
        pnlCampos.add(lblOperacion);

        cmbOperacion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        cmbOperacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOperacionActionPerformed(evt);
            }
        });
        pnlCampos.add(cmbOperacion);

        lblRuta.setText("Ruta del archivo o directorio");
        pnlCampos.add(lblRuta);

        pnlCampos.add(txtRuta);

        lblTipo.setText("Que se crea");
        pnlCampos.add(lblTipo);

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        cmbTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoActionPerformed(evt);
            }
        });
        pnlCampos.add(cmbTipo);

        lblTamano.setText("Tamano en bloques");
        pnlCampos.add(lblTamano);

        txtTamano.setText("1");
        pnlCampos.add(txtTamano);

        lblPermiso.setText("Permiso");
        pnlCampos.add(lblPermiso);

        cmbPermiso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        pnlCampos.add(cmbPermiso);

        lblDueno.setText("Dueno (vacio = usuario actual)");
        pnlCampos.add(lblDueno);

        pnlCampos.add(txtDueno);

        lblNuevoNombre.setText("Nombre nuevo (vacio = no renombrar)");
        pnlCampos.add(lblNuevoNombre);

        pnlCampos.add(txtNuevoNombre);

        lblMemoria.setText("Memoria (MB)");
        pnlCampos.add(lblMemoria);

        txtMemoria.setText("16");
        pnlCampos.add(txtMemoria);

        lblInstrucciones.setText("Instrucciones");
        pnlCampos.add(lblInstrucciones);

        txtInstrucciones.setText("2");
        pnlCampos.add(txtInstrucciones);

        lblPrioridad.setText("Prioridad (1 a 10)");
        pnlCampos.add(lblPrioridad);

        txtPrioridad.setText("1");
        pnlCampos.add(txtPrioridad);

        lblDeadline.setText("Deadline en ciclos (0 = sin deadline)");
        pnlCampos.add(lblDeadline);

        txtDeadline.setText("0");
        pnlCampos.add(txtDeadline);

        getContentPane().add(pnlCampos, java.awt.BorderLayout.CENTER);

        pnlAbajo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlAbajo.setLayout(new java.awt.GridLayout(3, 1, 0, 4));

        lblAyuda.setText(" ");
        pnlAbajo.add(lblAyuda);

        lblMensaje.setText(" ");
        pnlAbajo.add(lblMensaje);

        btnCrear.setText("Crear proceso");
        btnCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearActionPerformed(evt);
            }
        });
        pnlAbajo.add(btnCrear);

        getContentPane().add(pnlAbajo, java.awt.BorderLayout.PAGE_END);

        pack();
    }

    // Variables declaration - do not modify
    private javax.swing.JButton btnCrear;
    private javax.swing.JComboBox<String> cmbOperacion;
    private javax.swing.JComboBox<String> cmbPermiso;
    private javax.swing.JComboBox<String> cmbTipo;
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblDeadline;
    private javax.swing.JLabel lblDueno;
    private javax.swing.JLabel lblInstrucciones;
    private javax.swing.JLabel lblMemoria;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblNuevoNombre;
    private javax.swing.JLabel lblOperacion;
    private javax.swing.JLabel lblPermiso;
    private javax.swing.JLabel lblPrioridad;
    private javax.swing.JLabel lblRuta;
    private javax.swing.JLabel lblTamano;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JPanel pnlAbajo;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JTextField txtDeadline;
    private javax.swing.JTextField txtDueno;
    private javax.swing.JTextField txtInstrucciones;
    private javax.swing.JTextField txtMemoria;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNuevoNombre;
    private javax.swing.JTextField txtPrioridad;
    private javax.swing.JTextField txtRuta;
    private javax.swing.JTextField txtTamano;
    // End of variables declaration
    //--- fin del diseno ---
}
