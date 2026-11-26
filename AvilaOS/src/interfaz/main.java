/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import Data_Structures.Computador;
import Data_Structures.PCB;
import avilaos.Disco;
import avilaos.InfoNodo;
import avilaos.ModoUsuario;
import avilaos.PoliticaDisco;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import avilaos.Validaciones;
import java.awt.Color;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
//--- fin del bloque a: imports ---

/**
 *
 * @author vyckh
 */
//la ventana principal: mira al sistema operativo y lo dibuja, y le pasa lo que pide el usuario
//aqui no hay logica del simulador
public class main extends javax.swing.JFrame {

    //--- bloque a: campos ---
    private SistemaOperativo so;
    private Computador computador;
    private Disco disco;

    //las ventanas que se abren con los botones de abajo, se crean la primera vez que se piden
    private Panelpolitica panelPolitica;
    private Panellog panelLog;
    private PanelMetricas panelMetricas;
    private FormularioES formulario;
    private JDialog ventanaPolitica;
    private VentanaLog ventanaLog;
    private VentanaMetricas ventanaMetricas;

    //el proceso que el usuario escogio con un clic; si no hay, se muestra el que esta en la CPU
    private PCB seleccionado;
    //true mientras la ventana cambia un selector por su cuenta, para que no cuente como un clic
    private boolean ajustando = false;
    private Timer refresco;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form main
     */
    public main(SistemaOperativo sistema) {
        initComponents();
        //--- bloque b ---
        so = sistema;
        computador = so.getComputador();
        disco = so.getDisco();
        //el reloj queda detenido hasta que aprieten Iniciar
        so.pausar();
        so.iniciar();

        panelColas.setSeleccionListener(p -> seleccionado = (p == seleccionado ? null : p));
        panelArchivos.setAccionListener(this::abrirFormulario);

        txtDuracion.setText(String.valueOf(so.getDuracionCiclo()));
        txtVelocidad.setText(String.valueOf(disco.getVelocidad()));
        ajustando = true;
        PoliticaDisco[] politicas = PoliticaDisco.values();
        for (int i = 0; i < politicas.length; i++) {
            cmbPoliticaDisco.addItem(politicas[i]);
        }
        ModoUsuario[] modos = ModoUsuario.values();
        for (int i = 0; i < modos.length; i++) {
            cmbModo.addItem(modos[i]);
        }
        cmbPoliticaDisco.setSelectedItem(disco.getPolitica());
        String[] usuarios = so.getUsuarios();
        for (int i = 0; i < usuarios.length; i++) {
            cmbUsuario.addItem(usuarios[i]);
        }
        cmbUsuario.setEnabled(false);
        ajustando = false;
        mostrarModoUsuario();

        setSize(1440, 860);
        setLocationRelativeTo(null);
        //la ventana se refresca sola varias veces por segundo, asi tambien se actualiza con el reloj en pausa
        refresco = new Timer(150, e -> refrescar());
        refresco.start();
        refrescar();
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    //----- refresco: la ventana pide copias y las dibuja -----

    private void refrescar() {
        long ciclo = so.getCiclo();
        lblCiclo.setText(String.valueOf(ciclo));
        panelColas.mostrar(computador.copiarColas());
        panelPcb.mostrar(seleccionado != null ? seleccionado : computador.getEnEjecucion());
        lblModoCpu.setText(String.valueOf(computador.getmode()));
        int total = (int) computador.getRamt();
        int usada = (int) (computador.getRamt() - computador.getRam());
        barraRam.setMaximum(total <= 0 ? 1 : total);
        barraRam.setValue(usada);
        lblRam.setText(usada + " / " + total + " MB");

        InfoNodo[] arbol = so.copiarArbol();
        panelArchivos.mostrar(arbol, so.getVersionArchivos());
        panelTablas.mostrarAsignacion(arbol);
        panelTablas.mostrarCola(disco, ciclo);
        panelDisco.mostrar(disco);
        lblUltimo.setText(so.getLog().getUltimo());

        //si la politica cambio por otro lado, el selector se pone al dia
        if (cmbPoliticaDisco.getSelectedItem() != disco.getPolitica() && !cmbPoliticaDisco.isPopupVisible()) {
            ajustando = true;
            cmbPoliticaDisco.setSelectedItem(disco.getPolitica());
            ajustando = false;
        }
    }

    //----- lo que pide el usuario -----

    private void btnPausaActionPerformed(java.awt.event.ActionEvent evt) {
        if (so.estaPausado()) {
            so.reanudar();
            btnPausa.setText("Pausar");
        } else {
            so.pausar();
            btnPausa.setText("Reanudar");
        }
    }

    private void btnDuracionActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            so.setDuracionCiclo(Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 1, 60000));
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void btnVelocidadActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            so.setVelocidadCabezal(Validaciones.leerEntero(txtVelocidad.getText(), "Velocidad del cabezal", 1, 500));
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void cmbPoliticaDiscoActionPerformed(java.awt.event.ActionEvent evt) {
        if (ajustando || so == null) {
            return;
        }
        PoliticaDisco p = (PoliticaDisco) cmbPoliticaDisco.getSelectedItem();
        if (p != null && p != disco.getPolitica()) {
            so.cambiarPoliticaDisco(p);
        }
    }

    private void cmbModoActionPerformed(java.awt.event.ActionEvent evt) {
        cambiarModoUsuario();
    }

    private void cmbUsuarioActionPerformed(java.awt.event.ActionEvent evt) {
        cambiarModoUsuario();
    }

    private void cambiarModoUsuario() {
        if (ajustando || so == null) {
            return;
        }
        ModoUsuario modo = (ModoUsuario) cmbModo.getSelectedItem();
        cmbUsuario.setEnabled(modo == ModoUsuario.USUARIO);
        String nombre = "admin";
        if (modo == ModoUsuario.USUARIO) {
            Object elegido = cmbUsuario.getEditor().getItem();
            try {
                nombre = Validaciones.leerTexto(elegido == null ? "" : elegido.toString(), "Usuario", 30);
            } catch (IllegalArgumentException e) {
                //sin un nombre valido no se puede entrar como usuario, se queda en administrador
                avisar(e.getMessage());
                ajustando = true;
                cmbModo.setSelectedItem(ModoUsuario.ADMINISTRADOR);
                cmbUsuario.setEnabled(false);
                ajustando = false;
                modo = ModoUsuario.ADMINISTRADOR;
            }
        }
        if (modo != so.getModoUsuario() || !nombre.equals(so.getUsuario())) {
            so.setModoUsuario(modo, nombre);
        }
        mostrarModoUsuario();
    }

    private void mostrarModoUsuario() {
        boolean admin = so.getModoUsuario() == ModoUsuario.ADMINISTRADOR;
        lblModoUsuario.setText("Activo: " + so.getModoUsuario() + (admin ? "" : " (" + so.getUsuario() + ")"));
        lblModoUsuario.setForeground(admin ? new Color(20, 100, 60) : new Color(170, 90, 0));
    }

    private void btnCrearProcesoActionPerformed(java.awt.event.ActionEvent evt) {
        abrirFormulario(null, panelArchivos.getSeleccionado(), false);
    }

    //abre el formulario ya preparado para la operacion; el formulario solo crea el proceso
    private void abrirFormulario(TipoOperacionES op, InfoNodo sobre, boolean directorio) {
        if (op != null && op != TipoOperacionES.CREAR && sobre == null) {
            avisar("Primero selecciona un archivo o directorio en el arbol");
            return;
        }
        if (formulario == null) {
            formulario = new FormularioES(this, so);
        }
        formulario.preparar(op, sobre, directorio);
        formulario.setVisible(true);
        formulario.toFront();
    }

    private void btnPoliticaCpuActionPerformed(java.awt.event.ActionEvent evt) {
        if (ventanaPolitica == null) {
            panelPolitica = new Panelpolitica();
            panelPolitica.setComputador(computador);
            ventanaPolitica = new JDialog(this, "Politica de CPU", false);
            ventanaPolitica.add(panelPolitica);
            ventanaPolitica.pack();
            ventanaPolitica.setLocationRelativeTo(this);
        }
        panelPolitica.mostrarPolitica();
        ventanaPolitica.setVisible(true);
    }

    private void btnLogActionPerformed(java.awt.event.ActionEvent evt) {
        if (ventanaLog == null) {
            panelLog = new Panellog(so.getLog());
            ventanaLog = new VentanaLog(panelLog);
            ventanaLog.setLocationRelativeTo(this);
        }
        ventanaLog.setVisible(true);
        ventanaLog.toFront();
    }

    private void btnMetricasActionPerformed(java.awt.event.ActionEvent evt) {
        if (ventanaMetricas == null) {
            panelMetricas = new PanelMetricas(so.getMetricas(), so.getDisco());
            ventanaMetricas = new VentanaMetricas(panelMetricas);
            ventanaMetricas.setLocationRelativeTo(this);
        }
        ventanaMetricas.setVisible(true);
        ventanaMetricas.toFront();
    }

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser selector = new JFileChooser(new java.io.File("datos").getAbsoluteFile());
        selector.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String ruta = selector.getSelectedFile().getPath();
        if (!ruta.toLowerCase().endsWith(".json")) {
            ruta = ruta + ".json";
        }
        try {
            so.guardarArchivos(ruta);
            avisar("Sistema de archivos guardado en " + ruta);
        } catch (IOException e) {
            avisar("No se pudo guardar: " + e.getMessage());
        }
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        so.detener();
        dispose();
        System.exit(0);
    }

    //los avisos no detienen el reloj, la simulacion sigue mientras el mensaje esta abierto
    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        pnlBarra = new javax.swing.JPanel();
        pnlFila1 = new javax.swing.JPanel();
        lblTextoCiclo = new javax.swing.JLabel();
        lblCiclo = new javax.swing.JLabel();
        btnPausa = new javax.swing.JButton();
        lblTextoDuracion = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        btnDuracion = new javax.swing.JButton();
        lblTextoModoCpu = new javax.swing.JLabel();
        lblModoCpu = new javax.swing.JLabel();
        lblTextoRam = new javax.swing.JLabel();
        barraRam = new javax.swing.JProgressBar();
        lblRam = new javax.swing.JLabel();
        pnlFila2 = new javax.swing.JPanel();
        lblTextoPolitica = new javax.swing.JLabel();
        cmbPoliticaDisco = new javax.swing.JComboBox<>();
        lblTextoVelocidad = new javax.swing.JLabel();
        txtVelocidad = new javax.swing.JTextField();
        btnVelocidad = new javax.swing.JButton();
        lblTextoModo = new javax.swing.JLabel();
        cmbModo = new javax.swing.JComboBox<>();
        lblTextoUsuario = new javax.swing.JLabel();
        cmbUsuario = new javax.swing.JComboBox<>();
        lblModoUsuario = new javax.swing.JLabel();
        splCentro = new javax.swing.JSplitPane();
        pnlArriba = new javax.swing.JPanel();
        panelColas = new interfaz.PanelColas();
        panelPcb = new interfaz.panelPCB();
        pnlAbajo = new javax.swing.JPanel();
        panelArchivos = new interfaz.PanelArchivos();
        panelDisco = new interfaz.PanelDisco();
        panelTablas = new interfaz.PanelTablas();
        pnlBotones = new javax.swing.JPanel();
        pnlListaBotones = new javax.swing.JPanel();
        btnCrearProceso = new javax.swing.JButton();
        btnPoliticaCpu = new javax.swing.JButton();
        btnLog = new javax.swing.JButton();
        btnMetricas = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        lblUltimo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("AvilaOS");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout(0, 4));

        pnlBarra.setLayout(new java.awt.GridLayout(2, 1));

        pnlFila1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 3));

        lblTextoCiclo.setText("Ciclo:");
        pnlFila1.add(lblTextoCiclo);

        lblCiclo.setFont(lblCiclo.getFont().deriveFont(lblCiclo.getFont().getStyle() | java.awt.Font.BOLD, lblCiclo.getFont().getSize()+4));
        lblCiclo.setText("0");
        lblCiclo.setPreferredSize(new java.awt.Dimension(70, 22));
        pnlFila1.add(lblCiclo);

        btnPausa.setText("Iniciar");
        btnPausa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPausaActionPerformed(evt);
            }
        });
        pnlFila1.add(btnPausa);

        lblTextoDuracion.setText("Duracion del ciclo (ms):");
        pnlFila1.add(lblTextoDuracion);

        txtDuracion.setColumns(5);
        pnlFila1.add(txtDuracion);

        btnDuracion.setText("Aplicar");
        btnDuracion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDuracionActionPerformed(evt);
            }
        });
        pnlFila1.add(btnDuracion);

        lblTextoModoCpu.setText("   CPU en modo:");
        pnlFila1.add(lblTextoModoCpu);

        lblModoCpu.setPreferredSize(new java.awt.Dimension(70, 20));
        pnlFila1.add(lblModoCpu);

        lblTextoRam.setText("RAM:");
        pnlFila1.add(lblTextoRam);

        barraRam.setPreferredSize(new java.awt.Dimension(160, 16));
        pnlFila1.add(barraRam);

        lblRam.setText(" ");
        pnlFila1.add(lblRam);

        pnlBarra.add(pnlFila1);

        pnlFila2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 3));

        lblTextoPolitica.setText("Politica de disco:");
        pnlFila2.add(lblTextoPolitica);

        cmbPoliticaDisco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPoliticaDiscoActionPerformed(evt);
            }
        });
        pnlFila2.add(cmbPoliticaDisco);

        lblTextoVelocidad.setText("Velocidad del cabezal (bloques por ciclo):");
        pnlFila2.add(lblTextoVelocidad);

        txtVelocidad.setColumns(3);
        pnlFila2.add(txtVelocidad);

        btnVelocidad.setText("Aplicar");
        btnVelocidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVelocidadActionPerformed(evt);
            }
        });
        pnlFila2.add(btnVelocidad);

        lblTextoModo.setText("   Modo de uso:");
        pnlFila2.add(lblTextoModo);

        cmbModo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbModoActionPerformed(evt);
            }
        });
        pnlFila2.add(cmbModo);

        lblTextoUsuario.setText("Usuario:");
        pnlFila2.add(lblTextoUsuario);

        cmbUsuario.setEditable(true);
        cmbUsuario.setPreferredSize(new java.awt.Dimension(120, 26));
        cmbUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbUsuarioActionPerformed(evt);
            }
        });
        pnlFila2.add(cmbUsuario);

        lblModoUsuario.setFont(lblModoUsuario.getFont().deriveFont(lblModoUsuario.getFont().getStyle() | java.awt.Font.BOLD));
        lblModoUsuario.setText(" ");
        pnlFila2.add(lblModoUsuario);

        pnlBarra.add(pnlFila2);

        getContentPane().add(pnlBarra, java.awt.BorderLayout.PAGE_START);

        splCentro.setDividerLocation(285);
        splCentro.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        pnlArriba.setLayout(new java.awt.BorderLayout(6, 0));
        pnlArriba.add(panelColas, java.awt.BorderLayout.CENTER);

        panelPcb.setPreferredSize(new java.awt.Dimension(290, 240));
        pnlArriba.add(panelPcb, java.awt.BorderLayout.LINE_END);

        splCentro.setTopComponent(pnlArriba);

        pnlAbajo.setLayout(new java.awt.BorderLayout(6, 0));
        pnlAbajo.add(panelArchivos, java.awt.BorderLayout.LINE_START);
        pnlAbajo.add(panelDisco, java.awt.BorderLayout.CENTER);
        pnlAbajo.add(panelTablas, java.awt.BorderLayout.LINE_END);

        splCentro.setRightComponent(pnlAbajo);

        getContentPane().add(splCentro, java.awt.BorderLayout.CENTER);

        pnlBotones.setLayout(new java.awt.BorderLayout());

        pnlListaBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));

        btnCrearProceso.setText("Crear proceso");
        btnCrearProceso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCrearProcesoActionPerformed(evt);
            }
        });
        pnlListaBotones.add(btnCrearProceso);

        btnPoliticaCpu.setText("Politica de CPU");
        btnPoliticaCpu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPoliticaCpuActionPerformed(evt);
            }
        });
        pnlListaBotones.add(btnPoliticaCpu);

        btnLog.setText("Log");
        btnLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogActionPerformed(evt);
            }
        });
        pnlListaBotones.add(btnLog);

        btnMetricas.setText("Metricas");
        btnMetricas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMetricasActionPerformed(evt);
            }
        });
        pnlListaBotones.add(btnMetricas);

        btnGuardar.setText("Guardar sistema de archivos");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        pnlListaBotones.add(btnGuardar);

        pnlBotones.add(pnlListaBotones, java.awt.BorderLayout.LINE_START);

        lblUltimo.setForeground(new java.awt.Color(70, 70, 70));
        lblUltimo.setText(" ");
        lblUltimo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
        pnlBotones.add(lblUltimo, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlBotones, java.awt.BorderLayout.PAGE_END);

        pack();
    }

    // Variables declaration - do not modify
    private javax.swing.JProgressBar barraRam;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnDuracion;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLog;
    private javax.swing.JButton btnMetricas;
    private javax.swing.JButton btnPausa;
    private javax.swing.JButton btnPoliticaCpu;
    private javax.swing.JButton btnVelocidad;
    private javax.swing.JComboBox<ModoUsuario> cmbModo;
    private javax.swing.JComboBox<PoliticaDisco> cmbPoliticaDisco;
    private javax.swing.JComboBox<String> cmbUsuario;
    private javax.swing.JLabel lblCiclo;
    private javax.swing.JLabel lblModoCpu;
    private javax.swing.JLabel lblModoUsuario;
    private javax.swing.JLabel lblRam;
    private javax.swing.JLabel lblTextoCiclo;
    private javax.swing.JLabel lblTextoDuracion;
    private javax.swing.JLabel lblTextoModo;
    private javax.swing.JLabel lblTextoModoCpu;
    private javax.swing.JLabel lblTextoPolitica;
    private javax.swing.JLabel lblTextoRam;
    private javax.swing.JLabel lblTextoUsuario;
    private javax.swing.JLabel lblTextoVelocidad;
    private javax.swing.JLabel lblUltimo;
    private interfaz.PanelArchivos panelArchivos;
    private interfaz.PanelColas panelColas;
    private interfaz.PanelDisco panelDisco;
    private interfaz.panelPCB panelPcb;
    private interfaz.PanelTablas panelTablas;
    private javax.swing.JPanel pnlAbajo;
    private javax.swing.JPanel pnlArriba;
    private javax.swing.JPanel pnlBarra;
    private javax.swing.JPanel pnlBotones;
    private javax.swing.JPanel pnlFila1;
    private javax.swing.JPanel pnlFila2;
    private javax.swing.JPanel pnlListaBotones;
    private javax.swing.JSplitPane splCentro;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JTextField txtVelocidad;
    // End of variables declaration
    //--- fin del diseno ---
}
