/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import Data_Structures.Computador;
import Data_Structures.PCB;
import avilaos.Disco;
import avilaos.InfoNodo;
import avilaos.ModoUsuario;
import avilaos.PoliticaDisco;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import avilaos.Validaciones;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author vyckh
 */
//la ventana principal: mira al sistema operativo y lo dibuja, y le pasa lo que pide el usuario
//aqui no hay logica del simulador
public class main extends JFrame {

    private SistemaOperativo so;
    private Computador computador;
    private Disco disco;

    private PanelColas panelColas;
    private panelPCB panelPcb;
    private PanelArchivos panelArchivos;
    private PanelDisco panelDisco;
    private PanelTablas panelTablas;
    private Panelpolitica panelPolitica;
    private Panellog panelLog;
    private PanelMetricas panelMetricas;
    private FormularioES formulario;
    private JDialog ventanaPolitica;
    private VentanaLog ventanaLog;
    private VentanaMetricas ventanaMetricas;

    private JLabel lblCiclo = new JLabel("0");
    private JLabel lblModoCpu = new JLabel();
    private JLabel lblRam = new JLabel();
    private JProgressBar barraRam = new JProgressBar();
    private JLabel lblModoUsuario = new JLabel();
    private JLabel lblUltimo = new JLabel(" ");
    private JTextField txtDuracion = new JTextField(5);
    private JTextField txtVelocidad = new JTextField(3);
    private JButton btnPausa = new JButton("Iniciar");
    private JComboBox<PoliticaDisco> cmbPoliticaDisco = new JComboBox<>(PoliticaDisco.values());
    private JComboBox<ModoUsuario> cmbModo = new JComboBox<>(ModoUsuario.values());
    private JComboBox<String> cmbUsuario = new JComboBox<>();

    //el proceso que el usuario escogio con un clic; si no hay, se muestra el que esta en la CPU
    private PCB seleccionado;
    //true mientras la ventana cambia un selector por su cuenta, para que no cuente como un clic
    private boolean ajustando = false;
    private Timer refresco;

    public main(SistemaOperativo sistema) {
        super("AvilaOS");
        so = sistema;
        computador = so.getComputador();
        disco = so.getDisco();
        //el reloj queda detenido hasta que aprieten Iniciar
        so.pausar();
        so.iniciar();

        panelColas = new PanelColas(p -> seleccionado = (p == seleccionado ? null : p));
        panelPcb = new panelPCB();
        panelArchivos = new PanelArchivos(this::abrirFormulario);
        panelDisco = new PanelDisco();
        panelTablas = new PanelTablas();

        JPanel arriba = new JPanel(new BorderLayout(6, 0));
        arriba.add(panelColas, BorderLayout.CENTER);
        panelPcb.setPreferredSize(new Dimension(290, 240));
        arriba.add(panelPcb, BorderLayout.EAST);

        //abajo van el arbol, el disco y las tablas; el disco se queda con el espacio que sobre
        JPanel abajo = new JPanel(new BorderLayout(6, 0));
        abajo.add(panelArchivos, BorderLayout.WEST);
        abajo.add(panelDisco, BorderLayout.CENTER);
        abajo.add(panelTablas, BorderLayout.EAST);
        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        centro.setDividerLocation(285);

        setLayout(new BorderLayout(0, 4));
        add(armarBarra(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(armarBotones(), BorderLayout.SOUTH);

        txtDuracion.setText(String.valueOf(so.getDuracionCiclo()));
        txtVelocidad.setText(String.valueOf(disco.getVelocidad()));
        ajustando = true;
        cmbPoliticaDisco.setSelectedItem(disco.getPolitica());
        String[] usuarios = so.getUsuarios();
        for (int i = 0; i < usuarios.length; i++) {
            cmbUsuario.addItem(usuarios[i]);
        }
        cmbUsuario.setEditable(true);
        cmbUsuario.setEnabled(false);
        ajustando = false;
        mostrarModoUsuario();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                so.detener();
                dispose();
                System.exit(0);
            }
        });
        setSize(1440, 860);
        setLocationRelativeTo(null);

        //la ventana se refresca sola varias veces por segundo, asi tambien se actualiza con el reloj en pausa
        refresco = new Timer(150, e -> refrescar());
        refresco.start();
        refrescar();
    }

    //----- armado de la ventana -----

    private JPanel armarBarra() {
        JPanel barra = new JPanel(new GridLayout(2, 1));
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

        lblCiclo.setFont(lblCiclo.getFont().deriveFont(Font.BOLD, 16f));
        lblCiclo.setPreferredSize(new Dimension(70, 22));
        fila1.add(new JLabel("Ciclo:"));
        fila1.add(lblCiclo);
        btnPausa.addActionListener(e -> pausarOReanudar());
        fila1.add(btnPausa);
        fila1.add(new JLabel("Duracion del ciclo (ms):"));
        fila1.add(txtDuracion);
        JButton btnDuracion = new JButton("Aplicar");
        btnDuracion.addActionListener(e -> aplicarDuracion());
        fila1.add(btnDuracion);
        fila1.add(new JLabel("   CPU en modo:"));
        lblModoCpu.setPreferredSize(new Dimension(70, 20));
        fila1.add(lblModoCpu);
        fila1.add(new JLabel("RAM:"));
        barraRam.setPreferredSize(new Dimension(160, 16));
        fila1.add(barraRam);
        fila1.add(lblRam);

        fila2.add(new JLabel("Politica de disco:"));
        cmbPoliticaDisco.addActionListener(e -> cambiarPoliticaDisco());
        fila2.add(cmbPoliticaDisco);
        fila2.add(new JLabel("Velocidad del cabezal (bloques por ciclo):"));
        fila2.add(txtVelocidad);
        JButton btnVelocidad = new JButton("Aplicar");
        btnVelocidad.addActionListener(e -> aplicarVelocidad());
        fila2.add(btnVelocidad);
        fila2.add(new JLabel("   Modo de uso:"));
        cmbModo.addActionListener(e -> cambiarModoUsuario());
        fila2.add(cmbModo);
        fila2.add(new JLabel("Usuario:"));
        cmbUsuario.setPreferredSize(new Dimension(120, 26));
        cmbUsuario.addActionListener(e -> cambiarModoUsuario());
        fila2.add(cmbUsuario);
        lblModoUsuario.setFont(lblModoUsuario.getFont().deriveFont(Font.BOLD));
        fila2.add(lblModoUsuario);

        barra.add(fila1);
        barra.add(fila2);
        return barra;
    }

    private JPanel armarBotones() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.add(boton("Crear proceso", () -> abrirFormulario(null, panelArchivos.getSeleccionado(), false)));
        botones.add(boton("Politica de CPU", this::abrirPolitica));
        botones.add(boton("Log", this::abrirLog));
        botones.add(boton("Metricas", this::abrirMetricas));
        botones.add(boton("Guardar sistema de archivos", this::guardarArchivos));
        panel.add(botones, BorderLayout.WEST);
        lblUltimo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        lblUltimo.setForeground(new Color(70, 70, 70));
        panel.add(lblUltimo, BorderLayout.CENTER);
        return panel;
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> accion.run());
        return b;
    }

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

    private void pausarOReanudar() {
        if (so.estaPausado()) {
            so.reanudar();
            btnPausa.setText("Pausar");
        } else {
            so.pausar();
            btnPausa.setText("Reanudar");
        }
    }

    private void aplicarDuracion() {
        try {
            so.setDuracionCiclo(Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 1, 60000));
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void aplicarVelocidad() {
        try {
            so.setVelocidadCabezal(Validaciones.leerEntero(txtVelocidad.getText(), "Velocidad del cabezal", 1, 500));
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        }
    }

    private void cambiarPoliticaDisco() {
        if (ajustando) {
            return;
        }
        PoliticaDisco p = (PoliticaDisco) cmbPoliticaDisco.getSelectedItem();
        if (p != null && p != disco.getPolitica()) {
            so.cambiarPoliticaDisco(p);
        }
    }

    private void cambiarModoUsuario() {
        if (ajustando) {
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

    private void abrirPolitica() {
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

    private void abrirLog() {
        if (ventanaLog == null) {
            panelLog = new Panellog(so.getLog());
            ventanaLog = new VentanaLog(panelLog);
            ventanaLog.setLocationRelativeTo(this);
        }
        ventanaLog.setVisible(true);
        ventanaLog.toFront();
    }

    private void abrirMetricas() {
        if (ventanaMetricas == null) {
            panelMetricas = new PanelMetricas(so.getMetricas(), 1, 0);
            ventanaMetricas = new VentanaMetricas(panelMetricas);
            ventanaMetricas.setLocationRelativeTo(this);
        }
        ventanaMetricas.setVisible(true);
        ventanaMetricas.toFront();
    }

    private void guardarArchivos() {
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

    //los avisos no detienen el reloj, la simulacion sigue mientras el mensaje esta abierto
    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }
}
