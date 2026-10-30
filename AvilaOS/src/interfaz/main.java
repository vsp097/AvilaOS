/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package interfaz;

import Data_Structures.Cola;
import Data_Structures.Computador;
import Data_Structures.Listas;
import Data_Structures.Nodo;
import Data_Structures.PCB;
import avilaos.Buffer;
import avilaos.GlobalClock;
import avilaos.LogEventos;
import avilaos.Metricas;
import avilaos.SolicitudProceso;
import avilaos.Validaciones;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
/**
 *
 * @author diego
 */
public class main extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(main.class.getName());
    private Computador computador1;
    private Computador computador2;
    private GlobalClock gC;
    private LogEventos log;
    private Panellog panelLog;
    private panelPCB pcbComputador1;
    private panelPCB pcbComputador2;
    private JTabbedPane pestanas;
     private Listas<PanelBuffer> panelesBuffer = null;
    //los buffers creados, en el mismo orden en que los pide el formulario
    private Listas<Buffer> buffers = null;
    //el formulario de crear procesos y su ventana, se guardan para reusarlos
    private FormularioProceso formulario;
    private VistaProceso ventanaProceso;
    //el panel de crear buffers y su ventana
    private VistaBuffers panelBuffers;
    private VentanaBuffers ventanaBuffers;
    //unas solas metricas para los dos computadores y para el panel
    private Metricas metricas = new Metricas();
    private PanelMetricas panelMetricas;
    /**
     * Creates new form main
     */
    public main(Data_Structures.Computador c, Computador c2, GlobalClock g) {
                gC = g;
        initComponents();
        computador1 = c;
        computador2 = c2;
        Listas<Computador> compus = new Listas<>(new Nodo<Computador>(computador1));
        compus.insertar(new Nodo<Computador>(computador2));
        //la lista de computadores tiene que estar puesta antes de arrancar el reloj
        gC.setClocklist(compus);
        Cpu1.setText(c.getCPU());
        RAM1.setText((Float.toString(c.getRamt())));
        CPU2.setText(c2.getCPU());
        RAM2.setText((Float.toString(c2.getRamt())));
 
        //un solo log para los dos computadores
        log = new LogEventos();
        c.setLog(log);
        c2.setLog(log);
        computador1.setNombre("Computador 1");
        computador2.setNombre("Computador 2");
        //los dos reportan a la misma Metricas, cada uno con su numero
        computador1.setMetricas(metricas, 0);
        computador2.setMetricas(metricas, 1);
        armarPaneles();
 
        //cada computador tiene su panel de politica, que tambien deja cambiar el quantum
        Panelpolitica pol1 = new Panelpolitica();
        pol1.setComputador(computador1);
        politica1.add(pol1, BorderLayout.CENTER);
        Panelpolitica pol2 = new Panelpolitica();
        pol2.setComputador(computador2);
        politica2.add(pol2, BorderLayout.CENTER);
 
        gC.setClockListener(this::alCiclo);
        //el reloj arranca al final, cuando ya tiene todo conectado
        if (!gC.isAlive()) {
            gC.start();

        }

    }
    
    private void armarPaneles() {
        panelLog = new Panellog(log);
        pcbComputador1 = new panelPCB();
        pcbComputador2 = new panelPCB();
        pestanas = new JTabbedPane();
        pestanas.addTab("Log", new JScrollPane(panelLog));
        pestanas.addTab("PCB Computador 1", new JScrollPane(pcbComputador1));
        pestanas.addTab("PCB Computador 2", new JScrollPane(pcbComputador2));
        panelMetricas = new PanelMetricas(metricas, 2, 0);
        pestanas.addTab("Metricas", new JScrollPane(panelMetricas));
        pestanas.setPreferredSize(new Dimension(700, 240));
        getContentPane().add(pestanas, BorderLayout.SOUTH);
        pack();
    }

    public void agregarBuffer(Buffer b) {
        PanelBuffer pb = new PanelBuffer(b);
        Nodo<PanelBuffer> nodo = new Nodo<>(pb);
        if (panelesBuffer == null) {
            panelesBuffer = new Listas<>(nodo);
        } else {
            panelesBuffer.insertar(nodo);
        }
        pestanas.addTab(b.getNombre(), new JScrollPane(pb));
        //el buffer tambien se guarda para poder buscarlo por posicion
        Nodo<Buffer> nodoBuffer = new Nodo<>(b);
        if (buffers == null) {
            buffers = new Listas<>(nodoBuffer);
        } else {
            buffers.insertar(nodoBuffer);
        }
        //si el formulario ya esta abierto le aparece el buffer nuevo
        if (formulario != null) {
            formulario.actualizarBuffers(nombresBuffers());
        }
    }

    //devuelve el buffer que esta en esa posicion de la lista
    private Buffer buscarBuffer(int indice) {
        if (buffers == null || indice < 0 || indice >= buffers.len()) {
            return null;
        }
        return buffers.retornar(indice);
    }

    //los nombres de los buffers que ya existen, para el combo del formulario
    private String[] nombresBuffers() {
        int total = buffers == null ? 0 : buffers.len();
        String[] nombres = new String[total];
        for (int i = 0; i < total; i++) {
            nombres[i] = buffers.retornar(i).getNombre();
        }
        return nombres;
    }

    //abre el panel de crear buffers en su propia ventana, una sola vez
    private void abrirVistaBuffers() {
        if (ventanaBuffers == null) {
            Computador[] compus = {computador1, computador2};
            panelBuffers = new VistaBuffers(compus, this::bufferCreado);
            ventanaBuffers = new VentanaBuffers(panelBuffers);
            ventanaBuffers.setLocationRelativeTo(this);
        }
        ventanaBuffers.setVisible(true);
        ventanaBuffers.toFront();
    }

    //aqui llega el buffer que acaba de crear la vista de buffers
    private void bufferCreado(Buffer b, int indice) {
        agregarBuffer(b);
    }

    //abre el formulario de crear procesos en su propia ventana, una sola vez
    private void abrirFormularioProceso() {
        if (ventanaProceso == null) {
            formulario = new FormularioProceso(2, nombresBuffers(), this::procesoCreado);
            ventanaProceso = new VistaProceso(formulario);
            ventanaProceso.setLocationRelativeTo(this);
        }
        ventanaProceso.setVisible(true);
        ventanaProceso.toFront();
    }

    //aqui llega el proceso que armo el formulario y se mete en el computador que escogieron
    private void procesoCreado(SolicitudProceso solicitud) {
        PCB p = solicitud.getPcb();
        String tipo = p.gettype();
        if (tipo.equals("Productor") || tipo.equals("Consumidor")) {
            Buffer b = buscarBuffer(solicitud.getBuffer());
            if (b == null) {
                JOptionPane.showMessageDialog(this, "Ese buffer ya no existe", "Dato invalido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            p.setBuffer(b);
            p.setIndiceBuffer(solicitud.getBuffer());
            p.setElementos(solicitud.getElementos());
            p.setCadaCuantos(solicitud.getCadaCuantosCiclos());
        } else {
            //cpu bound e i/o bound terminan cuando se les acaban los ciclos
            p.settime(solicitud.getCiclos());
        }
        Computador destino = solicitud.getComputador() == 2 ? computador2 : computador1;
        destino.agregarProceso(p);
    }


 
    //esto lo llama el reloj en su propio hilo, justo despues de que los computadores hicieron su tick
    //por eso aqui es seguro recorrer las colas, y a la ventana solo le paso los modelos ya armados
    private void alCiclo(long cycle) {
        final Listas<String> nuevos1;
        final Listas<String> listos1;
        final Listas<String> bloqueados1;
        final Listas<String> terminados1;
        final Listas<String> nuevos2;
        final Listas<String> listos2;
        final Listas<String> bloqueados2;
        final Listas<String> terminados2;
        //mismo semaforo que usan onTick(), admitir() y agregarProceso(),
        //para que nadie encole a mitad de la copia
        computador1.tomar();
        try {
            nuevos1 = aListas(computador1.getNuevos());
            listos1 = aListas(computador1.getListos());
            bloqueados1 = aListasMotivo(computador1.getBloqueados());
            terminados1 = aListas(computador1.getTerminados());
        } finally {
            computador1.soltar();
        }
        computador2.tomar();
        try {
            nuevos2 = aListas(computador2.getNuevos());
            listos2 = aListas(computador2.getListos());
            bloqueados2 = aListasMotivo(computador2.getBloqueados());
            terminados2 = aListas(computador2.getTerminados());
        } finally {
            computador2.soltar();
        }
        SwingUtilities.invokeLater(() -> {
            clock.setText(String.valueOf(cycle));
            //las 4 colas del computador 1
            mostrar(jList2, nuevos1);
            mostrar(jList3, listos1);
            mostrar(jList5, bloqueados1);
            mostrar(jList6, terminados1);
            //las 4 colas del computador 2
            mostrar(jList4, nuevos2);
            mostrar(jList1, listos2);
            mostrar(jList7, bloqueados2);
            mostrar(jList8, terminados2);
            //la ram y el modo de cada computador
            RAM1.setText(textoRam(computador1));
            RAM2.setText(textoRam(computador2));
            modo1.setText("Modo: " + computador1.getmode());
            modo2.setText("Modo: " + computador2.getmode());
            //paneles: se actualizan en cada ciclo
            panelLog.actualizar();
            pcbComputador1.mostrar(computador1.getEnEjecucion());
            pcbComputador2.mostrar(computador2.getEnEjecucion());
            if (panelesBuffer != null) {
                for (int i = 0; i < panelesBuffer.len(); i++) {
                    panelesBuffer.retornar(i).actualizar();
                }
            }
        });
    }

    //la ram total, la que esta usada y la que queda libre
    private static String textoRam(Computador c) {
        float libre = c.getRam();
        float total = c.getRamt();
        return "total " + total + "   usada " + (total - libre) + "   libre " + libre;
    }

    //igual que aListas pero a los bloqueados les pone el motivo
    static Listas aListasMotivo(Cola<PCB> cola) {
        Listas<String> lista = null;
        Cola<PCB> temporal = new Cola<>();
        PCB p = cola.Desencolar();
        while (p != null) {
            Nodo<String> nodo = new Nodo<>(p.getname() + "  (" + p.getMotivo() + ")");
            if (lista == null) {
                lista = new Listas<>(nodo);
            } else {
                lista.insertar(nodo);
            }
            temporal.Encolar(p);
            p = cola.Desencolar();
        }
        p = temporal.Desencolar();
        while (p != null) {
            cola.Encolar(p);
            p = temporal.Desencolar();
        }
        return lista;
    }

    //copia el contenido de una cola a un modelo de lista sin cambiarle el orden a la cola
    static Listas aListas(Cola <PCB> cola) {
        Listas<String> lista = null;
        Cola<PCB> temporal = new Cola<>();
        PCB p = cola.Desencolar();
        while (p != null) {
            Nodo<String> nodo = new Nodo<>(p.getname() + "  (PC=" + p.getpc() + ")");
            if (lista == null) {
                lista = new Listas<>(nodo);
            } else {
                lista.insertar(nodo);
            }
            temporal.Encolar(p);
            p = cola.Desencolar();
        }
        p = temporal.Desencolar();
        while (p != null) {
            cola.Encolar(p);
            p = temporal.Desencolar();
        }
        return lista;

    }

    
    static void mostrar(javax.swing.JList<String> jl, Listas<String> lista) {
        String[] textos = new String[lista == null ? 0 : lista.len()];
        for (int i = 0; i < textos.length; i++) {
            textos[i] = lista.retornar(i);
        }
        jl.setListData(textos);

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane5 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        RAM1 = new javax.swing.JLabel();
        Cpu1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        RAM2 = new javax.swing.JLabel();
        CPU2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList4 = new javax.swing.JList<>();
        jScrollPane9 = new javax.swing.JScrollPane();
        jList5 = new javax.swing.JList<>();
        jScrollPane10 = new javax.swing.JScrollPane();
        jList6 = new javax.swing.JList<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        jList7 = new javax.swing.JList<>();
        jScrollPane12 = new javax.swing.JScrollPane();
        jList8 = new javax.swing.JList<>();
        politica1 = new javax.swing.JPanel();
        politica2 = new javax.swing.JPanel();
        modo1 = new javax.swing.JLabel();
        modo2 = new javax.swing.JLabel();
        lblDuracion = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        btnDuracion = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        clock = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        vista1 = new javax.swing.JMenu();
        vista2 = new javax.swing.JMenu();

        jScrollPane5.setViewportView(jEditorPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setMaximumSize(getMaximumSize());

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel1.setText("Computador 1");

        jLabel2.setText("Computador 2");

        jLabel3.setText("Sincronizador");

        jLabel4.setText("Scheduler");

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel5.setText("Scheduler");

        jLabel6.setText("Sincronizador");



        jLabel7.setText("CPU:");

        jLabel8.setText("Memoria Principal (RAM):");

        RAM1.setText(".");

        Cpu1.setText(".");

        jLabel11.setText("Cpu:");

        jLabel12.setText("Memoria Principal (RAM):");

        RAM2.setText(".");

        CPU2.setText(".");

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Listos"));
        jScrollPane1.setViewportView(jList1);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevos"));
        jScrollPane2.setViewportView(jList2);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Listos"));
        jScrollPane3.setViewportView(jList3);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevos"));
        jScrollPane4.setViewportView(jList4);

        jScrollPane9.setBorder(javax.swing.BorderFactory.createTitledBorder("Bloqueados"));
        jScrollPane9.setViewportView(jList5);

        jScrollPane10.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminados"));
        jScrollPane10.setViewportView(jList6);

        jScrollPane11.setBorder(javax.swing.BorderFactory.createTitledBorder("Bloqueados"));
        jScrollPane11.setViewportView(jList7);

        jScrollPane12.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminados"));
        jScrollPane12.setViewportView(jList8);

        politica1.setBorder(javax.swing.BorderFactory.createTitledBorder("Politica Computador 1"));
        politica1.setLayout(new java.awt.BorderLayout());

        politica2.setBorder(javax.swing.BorderFactory.createTitledBorder("Politica Computador 2"));
        politica2.setLayout(new java.awt.BorderLayout());

        modo1.setText("Modo:");

        modo2.setText("Modo:");

        lblDuracion.setText("Duracion del ciclo (ms):");

        txtDuracion.setText("1000");

        btnDuracion.setText("Aplicar");
        btnDuracion.addActionListener(this::btnDuracionActionPerformed);

        jLabel13.setText("Ciclos del Reloj:");

        clock.setText(".");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(400, 400, 400)
                .addComponent(jLabel13)
                .addGap(10, 10, 10)
                .addComponent(clock, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(Cpu1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel12))
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(490, 490, 490)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 930, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(660, 660, 660)
                .addComponent(RAM2, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(232, 232, 232)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(260, 260, 260)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel7))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(740, 740, 740)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(530, 530, 530)
                .addComponent(CPU2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel8))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(192, 192, 192)
                .addComponent(RAM1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(510, 510, 510)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(500, 500, 500)
                .addComponent(jLabel11))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(470, 470, 470)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(720, 720, 720)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 940, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 930, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(730, 730, 730)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(260, 260, 260)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(510, 510, 510)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(740, 740, 740)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(politica1, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(510, 510, 510)
                .addComponent(politica2, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(250, 250, 250)
                .addComponent(modo1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(700, 700, 700)
                .addComponent(modo2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(txtDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(clock, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDuracion)
                    .addComponent(btnDuracion))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(Cpu1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(100, 100, 100)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jLabel12)
                        .addGap(22, 22, 22)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(RAM2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel7))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(CPU2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(jLabel8))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(RAM1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(160, 160, 160)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel11))
                    .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(142, 142, 142)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(395, 395, 395)
                        .addComponent(politica1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(395, 395, 395)
                        .addComponent(politica2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(modo1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(modo2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    ))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jMenuBar1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        vista1.setText("vista 1");
        vista1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vista1MouseClicked(evt);
            }
        });
        jMenuBar1.add(vista1);

        vista2.setText("buffers");
        vista2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                vista2MouseClicked(evt);
            }
        });
        jMenuBar1.add(vista2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void vista2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vista2MouseClicked
        abrirVistaBuffers();
    }//GEN-LAST:event_vista2MouseClicked

    private void btnDuracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDuracionActionPerformed
        try {
            //la duracion del ciclo se puede cambiar con la simulacion corriendo
            int ms = Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 50, 10000);
            gC.setcduration(ms);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnDuracionActionPerformed

    private void vista1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_vista1MouseClicked
        abrirFormularioProceso();
    }//GEN-LAST:event_vista1MouseClicked



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CPU2;
    private javax.swing.JLabel Cpu1;
    private javax.swing.JLabel RAM1;
    private javax.swing.JLabel RAM2;
    private javax.swing.JLabel clock;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jList3;
    private javax.swing.JList<String> jList4;
    private javax.swing.JList<String> jList5;
    private javax.swing.JList<String> jList6;
    private javax.swing.JList<String> jList7;
    private javax.swing.JList<String> jList8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JLabel modo1;
    private javax.swing.JLabel modo2;
    private javax.swing.JLabel lblDuracion;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JButton btnDuracion;
    private javax.swing.JPanel politica1;
    private javax.swing.JPanel politica2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JMenu vista1;
    private javax.swing.JMenu vista2;
    // End of variables declaration//GEN-END:variables
}
