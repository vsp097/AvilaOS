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
import avilaos.Configuracion;
import avilaos.GlobalClock;
import avilaos.LogEventos;
import avilaos.Metricas;
import avilaos.PoliticaPlanificacion;
import avilaos.SelectorPolitica;
import avilaos.SolicitudProceso;
import avilaos.Validaciones;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
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
    private Listas<PanelBuffer> panelesBuffer = null;
    //los buffers creados, en el mismo orden en que los pide el formulario
    private Listas<Buffer> buffers = null;
    //el formulario de crear procesos y su ventana, se guardan para reusarlos
    private FormularioProceso formulario;
    private VistaProceso ventanaProceso;
    //el panel de crear buffers y su ventana
    private VistaBuffers panelCrearBuffers;
    private VentanaBuffers ventanaBuffers;
    //los procesos que se ven en cada una de las 8 listas, en el mismo orden que la lista
    private PCB[][] enLista = new PCB[8][];
    //el proceso que el usuario escogio con un clic; si no hay, se sigue al de la CPU
    private PCB seleccionado1;
    private PCB seleccionado2;
    //el panel de configuracion y su ventana
    private VistaConfiguracion panelConfiguracion;
    private VentanaConfiguracion ventanaConfiguracion;
    //unas solas metricas para los dos computadores y para el panel
    private Metricas metricas = new Metricas();
    private PanelMetricas panelMetricas;
    //las ventanas del log y de las metricas
    private VentanaLog ventanaLog;
    private VentanaMetricas ventanaMetricas;

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
        Cpu1.setText("CPU: " + c.getCPU());
        CPU2.setText("CPU: " + c2.getCPU());

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

        //al hacer clic en una cola se puede ver el PCB de ese proceso
        for (int i = 0; i < enLista.length; i++) {
            enLista[i] = new PCB[0];
        }
        conectarLista(jList2, 0, 1);
        conectarLista(jList3, 1, 1);
        conectarLista(jList5, 2, 1);
        conectarLista(jList6, 3, 1);
        conectarLista(jList4, 4, 2);
        conectarLista(jList1, 5, 2);
        conectarLista(jList7, 6, 2);
        conectarLista(jList8, 7, 2);

        txtDuracion.setText("1000");
        //la ventana abre centrada y con un tamano comodo
        setSize(1280, 760);
        setLocationRelativeTo(null);

        gC.setClockListener(this::alCiclo);
        //el reloj arranca al final, cuando ya tiene todo conectado
        if (!gC.isAlive()) {
            gC.start();
        }
    }

    //arma los paneles que viven dentro de la ventana y los que van en otras ventanas
    private void armarPaneles() {
        panelLog = new Panellog(log);
        pcbComputador1 = new panelPCB();
        pcbComputador2 = new panelPCB();
        pcb1Host.add(pcbComputador1, BorderLayout.CENTER);
        pcb2Host.add(pcbComputador2, BorderLayout.CENTER);
        //cada computador tiene su panel de politica, que tambien deja cambiar el quantum
        Panelpolitica pol1 = new Panelpolitica();
        pol1.setComputador(computador1);
        pol1Host.add(pol1, BorderLayout.CENTER);
        Panelpolitica pol2 = new Panelpolitica();
        pol2.setComputador(computador2);
        pol2Host.add(pol2, BorderLayout.CENTER);
        panelMetricas = new PanelMetricas(metricas, 2, 0);
    }

    public void agregarBuffer(Buffer b) {
        PanelBuffer pb = new PanelBuffer(b);
        Nodo<PanelBuffer> nodo = new Nodo<>(pb);
        if (panelesBuffer == null) {
            panelesBuffer = new Listas<>(nodo);
        } else {
            panelesBuffer.insertar(nodo);
        }
        //el aviso de que no hay buffers se quita con el primero
        sinBuffers.setVisible(false);
        filaBuffers.add(pb);
        filaBuffers.revalidate();
        filaBuffers.repaint();
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

    //abre una ventana y si ya estaba abierta solo la trae al frente
    private void mostrarVentana(javax.swing.JFrame v) {
        v.setVisible(true);
        v.toFront();
    }

    //abre la configuracion en su propia ventana, una sola vez
    private void abrirConfiguracion() {
        if (ventanaConfiguracion == null) {
            panelConfiguracion = new VistaConfiguracion(this::aplicarConfiguracion);
            ventanaConfiguracion = new VentanaConfiguracion(panelConfiguracion);
            ventanaConfiguracion.setLocationRelativeTo(this);
        }
        mostrarVentana(ventanaConfiguracion);
    }

    //abre el log en su propia ventana
    private void abrirLog() {
        if (ventanaLog == null) {
            ventanaLog = new VentanaLog(panelLog);
            ventanaLog.setLocationRelativeTo(this);
        }
        mostrarVentana(ventanaLog);
    }

    //abre las metricas en su propia ventana
    private void abrirMetricas() {
        if (ventanaMetricas == null) {
            ventanaMetricas = new VentanaMetricas(panelMetricas);
            ventanaMetricas.setLocationRelativeTo(this);
        }
        mostrarVentana(ventanaMetricas);
    }

    //la configuracion ya viene validada, aqui solo se reparte a quien le toca
    private void aplicarConfiguracion(Configuracion config) {
        gC.setcduration(config.duracionCicloMs);
        txtDuracion.setText(String.valueOf(config.duracionCicloMs));
        computador1.setRetrasoRed(config.latenciaRed);
        computador2.setRetrasoRed(config.latenciaRed);
        //validar() garantiza que hay por lo menos dos computadores
        aplicarAComputador(computador1, config.computadores[0]);
        aplicarAComputador(computador2, config.computadores[1]);
        //primero los buffers, porque los productores y consumidores los necesitan
        crearBuffers(config);
        crearProcesos(config);
        //se aplica una sola vez: hacerlo con procesos corriendo reiniciaria la RAM
        panelConfiguracion.desactivarAplicar();
        JOptionPane.showMessageDialog(this, "Configuracion aplicada");
    }

    //crea los buffers del archivo, en el mismo orden para que los procesos los encuentren
    private void crearBuffers(Configuracion config) {
        for (int i = 0; i < config.buffers.length; i++) {
            Configuracion.ConfigBuffer datos = config.buffers[i];
            Buffer b = new Buffer("B" + (i + 1), datos.capacidad);
            Computador anfitrion = datos.computadorAnfitrion == 2 ? computador2 : computador1;
            if (!b.setComputador(anfitrion)) {
                JOptionPane.showMessageDialog(this, "El buffer B" + (i + 1)
                        + " no cabe en la RAM del Computador " + datos.computadorAnfitrion,
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            //se agrega igual, para que las posiciones sigan cuadrando con el archivo
            agregarBuffer(b);
        }
    }

    //crea los procesos del archivo por el mismo camino que usa el formulario
    private void crearProcesos(Configuracion config) {
        for (int i = 0; i < config.procesos.length; i++) {
            Configuracion.ConfigProceso datos = config.procesos[i];
            PCB pcb = new PCB(FormularioProceso.siguienteId(), datos.nombre, 0, datos.tipo, datos.memoria);
            pcb.setPrioridad(datos.prioridad);
            pcb.setdeadline(datos.deadline);
            pcb.settime(datos.instrucciones);
            procesoCreado(new SolicitudProceso(pcb, datos.computador, datos.buffer,
                    datos.ciclos, datos.cadaCuantosCiclos, datos.elementos));
        }
    }

    //la ram, la politica y el quantum de un computador
    private void aplicarAComputador(Computador c, Configuracion.ConfigComputador datos) {
        c.setRAMt(datos.ram);
        PoliticaPlanificacion politica = PoliticaPlanificacion.valueOf(datos.politica);
        c.setPlanificador(SelectorPolitica.crear(politica, datos.quantum));
    }

    //abre el panel de crear buffers en su propia ventana, una sola vez
    private void abrirVistaBuffers() {
        if (ventanaBuffers == null) {
            Computador[] compus = {computador1, computador2};
            panelCrearBuffers = new VistaBuffers(compus, this::bufferCreado);
            ventanaBuffers = new VentanaBuffers(panelCrearBuffers);
            ventanaBuffers.setLocationRelativeTo(this);
        }
        mostrarVentana(ventanaBuffers);
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
        mostrarVentana(ventanaProceso);
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

    //un clic en la lista muestra el PCB de ese proceso; otro clic en el mismo vuelve a seguir la CPU
    private void conectarLista(javax.swing.JList<String> jl, int cual, int computador) {
        jl.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int i = jl.getSelectedIndex();
            //el refresco de cada ciclo borra la seleccion, y eso no es un clic del usuario
            if (i < 0 || i >= enLista[cual].length) {
                return;
            }
            PCB p = enLista[cual][i];
            if (computador == 1) {
                seleccionado1 = p == seleccionado1 ? null : p;
            } else {
                seleccionado2 = p == seleccionado2 ? null : p;
            }
        });
    }

    //copia los procesos de una cola a un arreglo, sin cambiarle el orden a la cola
    static PCB[] aArreglo(Cola<PCB> cola) {
        Cola<PCB> temporal = new Cola<>();
        int cuantos = 0;
        PCB p = cola.Desencolar();
        while (p != null) {
            temporal.Encolar(p);
            cuantos++;
            p = cola.Desencolar();
        }
        PCB[] arreglo = new PCB[cuantos];
        int i = 0;
        p = temporal.Desencolar();
        while (p != null) {
            arreglo[i] = p;
            i++;
            cola.Encolar(p);
            p = temporal.Desencolar();
        }
        return arreglo;
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
        //los mismos procesos, para poder mostrar su PCB si el usuario hace clic
        final PCB[][] copia = new PCB[8][];
        //mismo semaforo que usan onTick(), admitir() y agregarProceso(),
        //para que nadie encole a mitad de la copia
        computador1.tomar();
        try {
            nuevos1 = aListas(computador1.getNuevos());
            listos1 = aListas(computador1.getListos());
            bloqueados1 = aListasMotivo(computador1.getBloqueados());
            terminados1 = aListas(computador1.getTerminados());
            copia[0] = aArreglo(computador1.getNuevos());
            copia[1] = aArreglo(computador1.getListos());
            copia[2] = aArreglo(computador1.getBloqueados());
            copia[3] = aArreglo(computador1.getTerminados());
        } finally {
            computador1.soltar();
        }
        computador2.tomar();
        try {
            nuevos2 = aListas(computador2.getNuevos());
            listos2 = aListas(computador2.getListos());
            bloqueados2 = aListasMotivo(computador2.getBloqueados());
            terminados2 = aListas(computador2.getTerminados());
            copia[4] = aArreglo(computador2.getNuevos());
            copia[5] = aArreglo(computador2.getListos());
            copia[6] = aArreglo(computador2.getBloqueados());
            copia[7] = aArreglo(computador2.getTerminados());
        } finally {
            computador2.soltar();
        }
        SwingUtilities.invokeLater(() -> {
            enLista = copia;
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
            mostrarRam(computador1, barraRam1, RAM1);
            mostrarRam(computador2, barraRam2, RAM2);
            modo1.setText("Modo: " + computador1.getmode());
            modo2.setText("Modo: " + computador2.getmode());
            //el texto de ayuda cambia cuando ya hay procesos
            ayuda.setText(hayProcesos(copia)
                    ? "Haz clic en un proceso de cualquier cola para ver su PCB"
                    : "Crea un buffer (si vas a usar productores o consumidores) y luego crea procesos"
                    + " con los botones de abajo");
            //paneles: se actualizan en cada ciclo
            panelLog.actualizar();
            pcbComputador1.mostrar(seleccionado1 != null ? seleccionado1 : computador1.getEnEjecucion());
            pcbComputador2.mostrar(seleccionado2 != null ? seleccionado2 : computador2.getEnEjecucion());
            if (panelesBuffer != null) {
                for (int i = 0; i < panelesBuffer.len(); i++) {
                    panelesBuffer.retornar(i).actualizar();
                }
            }
        });
    }

    //true si hay por lo menos un proceso en alguna cola
    private static boolean hayProcesos(PCB[][] copia) {
        for (int i = 0; i < copia.length; i++) {
            if (copia[i] != null && copia[i].length > 0) {
                return true;
            }
        }
        return false;
    }

    //la barra y el texto de la memoria: cuanta esta usada de cuanta hay
    private static void mostrarRam(Computador c, javax.swing.JProgressBar barra, javax.swing.JLabel etiqueta) {
        int total = (int) c.getRamt();
        int usada = (int) (c.getRamt() - c.getRam());
        barra.setMaximum(total <= 0 ? 1 : total);
        barra.setValue(usada);
        etiqueta.setText(usada + " / " + total + " MB");
    }

    //copia el contenido de una cola a un modelo de lista sin cambiarle el orden a la cola
    static Listas aListas(Cola<PCB> cola) {
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

    //en la cola el motivo va corto para que quepa; el completo se ve en el PCB y en el log
    private static String motivoCorto(String motivo) {
        int i = motivo.indexOf(" (buffer");
        String corto = i < 0 ? motivo : motivo.substring(0, i);
        return corto.replace("semaforo ", "sem. ");
    }

    //igual que aListas pero a los bloqueados les pone el motivo
    static Listas aListasMotivo(Cola<PCB> cola) {
        Listas<String> lista = null;
        Cola<PCB> temporal = new Cola<>();
        PCB p = cola.Desencolar();
        while (p != null) {
            Nodo<String> nodo = new Nodo<>(p.getname() + "  (" + motivoCorto(p.getMotivo()) + ")");
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

        barraSuperior = new javax.swing.JPanel();
        filaCiclo = new javax.swing.JPanel();
        panelCiclo = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        clock = new javax.swing.JLabel();
        panelDuracion = new javax.swing.JPanel();
        lblDuracion = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        btnDuracion = new javax.swing.JButton();
        ayuda = new javax.swing.JLabel();
        panelComputadores = new javax.swing.JPanel();
        comp1 = new javax.swing.JPanel();
        arriba1 = new javax.swing.JPanel();
        hw1 = new javax.swing.JPanel();
        Cpu1 = new javax.swing.JLabel();
        modo1 = new javax.swing.JLabel();
        ramFila1 = new javax.swing.JPanel();
        barraRam1 = new javax.swing.JProgressBar();
        RAM1 = new javax.swing.JLabel();
        pcb1Host = new javax.swing.JPanel();
        pol1Host = new javax.swing.JPanel();
        colas1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList<>();
        jScrollPane9 = new javax.swing.JScrollPane();
        jList5 = new javax.swing.JList<>();
        jScrollPane10 = new javax.swing.JScrollPane();
        jList6 = new javax.swing.JList<>();
        comp2 = new javax.swing.JPanel();
        arriba2 = new javax.swing.JPanel();
        hw2 = new javax.swing.JPanel();
        CPU2 = new javax.swing.JLabel();
        modo2 = new javax.swing.JLabel();
        ramFila2 = new javax.swing.JPanel();
        barraRam2 = new javax.swing.JProgressBar();
        RAM2 = new javax.swing.JLabel();
        pcb2Host = new javax.swing.JPanel();
        pol2Host = new javax.swing.JPanel();
        colas2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList4 = new javax.swing.JList<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        jList7 = new javax.swing.JList<>();
        jScrollPane12 = new javax.swing.JScrollPane();
        jList8 = new javax.swing.JList<>();
        abajo = new javax.swing.JPanel();
        panelBuffers = new javax.swing.JPanel();
        scrollBuffers = new javax.swing.JScrollPane();
        filaBuffers = new javax.swing.JPanel();
        sinBuffers = new javax.swing.JLabel();
        barraNav = new javax.swing.JPanel();
        btnSimulacion = new javax.swing.JButton();
        btnCrearProceso = new javax.swing.JButton();
        btnCrearBuffer = new javax.swing.JButton();
        btnLog = new javax.swing.JButton();
        btnMetricas = new javax.swing.JButton();
        btnConfiguracion = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ÁvilaOS — Simulación");
        setMinimumSize(new java.awt.Dimension(1100, 700));

        barraSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 6, 10));
        barraSuperior.setLayout(new java.awt.BorderLayout());

        filaCiclo.setLayout(new java.awt.BorderLayout());

        panelCiclo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));

        jLabel13.setFont(jLabel13.getFont().deriveFont(jLabel13.getFont().getStyle() | java.awt.Font.BOLD, jLabel13.getFont().getSize()+8));
        jLabel13.setText("Ciclo:");
        panelCiclo.add(jLabel13);

        clock.setFont(clock.getFont().deriveFont(clock.getFont().getStyle() | java.awt.Font.BOLD, clock.getFont().getSize()+8));
        clock.setText("0");
        panelCiclo.add(clock);

        filaCiclo.add(panelCiclo, java.awt.BorderLayout.WEST);

        panelDuracion.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));

        lblDuracion.setText("Duración del ciclo (ms)");
        panelDuracion.add(lblDuracion);

        txtDuracion.setColumns(6);
        txtDuracion.setText("");
        panelDuracion.add(txtDuracion);

        btnDuracion.setText("Aplicar");
        btnDuracion.addActionListener(this::btnDuracionActionPerformed);
        panelDuracion.add(btnDuracion);

        filaCiclo.add(panelDuracion, java.awt.BorderLayout.EAST);

        barraSuperior.add(filaCiclo, java.awt.BorderLayout.CENTER);

        ayuda.setText("Crea un buffer (si vas a usar productores o consumidores) y luego crea procesos con los botones de abajo");
        barraSuperior.add(ayuda, java.awt.BorderLayout.SOUTH);

        getContentPane().add(barraSuperior, java.awt.BorderLayout.NORTH);

        panelComputadores.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panelComputadores.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        comp1.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 1"));
        comp1.setLayout(new java.awt.BorderLayout());

        arriba1.setLayout(new java.awt.BorderLayout());

        hw1.setBorder(javax.swing.BorderFactory.createTitledBorder("Hardware"));
        hw1.setLayout(new java.awt.GridLayout(3, 1));

        Cpu1.setText("CPU:");
        hw1.add(Cpu1);

        modo1.setText("Modo:");
        hw1.add(modo1);

        ramFila1.setLayout(new java.awt.BorderLayout());
        ramFila1.add(barraRam1, java.awt.BorderLayout.CENTER);

        RAM1.setText("0 / 0 MB");
        RAM1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 4));
        ramFila1.add(RAM1, java.awt.BorderLayout.EAST);

        hw1.add(ramFila1);

        arriba1.add(hw1, java.awt.BorderLayout.NORTH);

        pcb1Host.setLayout(new java.awt.BorderLayout());
        arriba1.add(pcb1Host, java.awt.BorderLayout.CENTER);

        pol1Host.setBorder(javax.swing.BorderFactory.createTitledBorder("Planificador"));
        pol1Host.setLayout(new java.awt.BorderLayout());
        arriba1.add(pol1Host, java.awt.BorderLayout.SOUTH);

        comp1.add(arriba1, java.awt.BorderLayout.NORTH);

        colas1.setBorder(javax.swing.BorderFactory.createTitledBorder("Colas"));
        colas1.setLayout(new java.awt.GridLayout(1, 4, 6, 0));

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevos"));
        jScrollPane2.setViewportView(jList2);
        colas1.add(jScrollPane2);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Listos"));
        jScrollPane3.setViewportView(jList3);
        colas1.add(jScrollPane3);

        jScrollPane9.setBorder(javax.swing.BorderFactory.createTitledBorder("Bloqueados"));
        jScrollPane9.setViewportView(jList5);
        colas1.add(jScrollPane9);

        jScrollPane10.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminados"));
        jScrollPane10.setViewportView(jList6);
        colas1.add(jScrollPane10);

        comp1.add(colas1, java.awt.BorderLayout.CENTER);

        panelComputadores.add(comp1);

        comp2.setBorder(javax.swing.BorderFactory.createTitledBorder("Computador 2"));
        comp2.setLayout(new java.awt.BorderLayout());

        arriba2.setLayout(new java.awt.BorderLayout());

        hw2.setBorder(javax.swing.BorderFactory.createTitledBorder("Hardware"));
        hw2.setLayout(new java.awt.GridLayout(3, 1));

        CPU2.setText("CPU:");
        hw2.add(CPU2);

        modo2.setText("Modo:");
        hw2.add(modo2);

        ramFila2.setLayout(new java.awt.BorderLayout());
        ramFila2.add(barraRam2, java.awt.BorderLayout.CENTER);

        RAM2.setText("0 / 0 MB");
        RAM2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 4));
        ramFila2.add(RAM2, java.awt.BorderLayout.EAST);

        hw2.add(ramFila2);

        arriba2.add(hw2, java.awt.BorderLayout.NORTH);

        pcb2Host.setLayout(new java.awt.BorderLayout());
        arriba2.add(pcb2Host, java.awt.BorderLayout.CENTER);

        pol2Host.setBorder(javax.swing.BorderFactory.createTitledBorder("Planificador"));
        pol2Host.setLayout(new java.awt.BorderLayout());
        arriba2.add(pol2Host, java.awt.BorderLayout.SOUTH);

        comp2.add(arriba2, java.awt.BorderLayout.NORTH);

        colas2.setBorder(javax.swing.BorderFactory.createTitledBorder("Colas"));
        colas2.setLayout(new java.awt.GridLayout(1, 4, 6, 0));

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Nuevos"));
        jScrollPane4.setViewportView(jList4);
        colas2.add(jScrollPane4);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Listos"));
        jScrollPane1.setViewportView(jList1);
        colas2.add(jScrollPane1);

        jScrollPane11.setBorder(javax.swing.BorderFactory.createTitledBorder("Bloqueados"));
        jScrollPane11.setViewportView(jList7);
        colas2.add(jScrollPane11);

        jScrollPane12.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminados"));
        jScrollPane12.setViewportView(jList8);
        colas2.add(jScrollPane12);

        comp2.add(colas2, java.awt.BorderLayout.CENTER);

        panelComputadores.add(comp2);

        getContentPane().add(panelComputadores, java.awt.BorderLayout.CENTER);

        abajo.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 10, 10));
        abajo.setLayout(new java.awt.BorderLayout());

        panelBuffers.setBorder(javax.swing.BorderFactory.createTitledBorder("Buffers"));
        panelBuffers.setPreferredSize(new java.awt.Dimension(100, 215));
        panelBuffers.setLayout(new java.awt.BorderLayout());

        scrollBuffers.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollBuffers.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollBuffers.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        filaBuffers.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        sinBuffers.setText("No hay buffers todavía");
        filaBuffers.add(sinBuffers);

        scrollBuffers.setViewportView(filaBuffers);

        panelBuffers.add(scrollBuffers, java.awt.BorderLayout.CENTER);

        abajo.add(panelBuffers, java.awt.BorderLayout.CENTER);

        barraNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        barraNav.setLayout(new java.awt.GridLayout(1, 6, 6, 0));

        btnSimulacion.setText("Simulación");
        btnSimulacion.addActionListener(this::btnSimulacionActionPerformed);
        barraNav.add(btnSimulacion);

        btnCrearProceso.setText("Crear proceso");
        btnCrearProceso.addActionListener(this::btnCrearProcesoActionPerformed);
        barraNav.add(btnCrearProceso);

        btnCrearBuffer.setText("Crear buffer");
        btnCrearBuffer.addActionListener(this::btnCrearBufferActionPerformed);
        barraNav.add(btnCrearBuffer);

        btnLog.setText("Log");
        btnLog.addActionListener(this::btnLogActionPerformed);
        barraNav.add(btnLog);

        btnMetricas.setText("Métricas");
        btnMetricas.addActionListener(this::btnMetricasActionPerformed);
        barraNav.add(btnMetricas);

        btnConfiguracion.setText("Configuración");
        btnConfiguracion.addActionListener(this::btnConfiguracionActionPerformed);
        barraNav.add(btnConfiguracion);

        abajo.add(barraNav, java.awt.BorderLayout.SOUTH);

        getContentPane().add(abajo, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDuracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDuracionActionPerformed
        try {
            //la duracion del ciclo se puede cambiar con la simulacion corriendo
            int ms = Validaciones.leerEntero(txtDuracion.getText(), "Duracion del ciclo", 50, 10000);
            gC.setcduration(ms);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnDuracionActionPerformed

    private void btnSimulacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimulacionActionPerformed
        toFront();
    }//GEN-LAST:event_btnSimulacionActionPerformed

    private void btnCrearProcesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearProcesoActionPerformed
        abrirFormularioProceso();
    }//GEN-LAST:event_btnCrearProcesoActionPerformed

    private void btnCrearBufferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearBufferActionPerformed
        abrirVistaBuffers();
    }//GEN-LAST:event_btnCrearBufferActionPerformed

    private void btnLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogActionPerformed
        abrirLog();
    }//GEN-LAST:event_btnLogActionPerformed

    private void btnMetricasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMetricasActionPerformed
        abrirMetricas();
    }//GEN-LAST:event_btnMetricasActionPerformed

    private void btnConfiguracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfiguracionActionPerformed
        abrirConfiguracion();
    }//GEN-LAST:event_btnConfiguracionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel CPU2;
    private javax.swing.JLabel Cpu1;
    private javax.swing.JLabel RAM1;
    private javax.swing.JLabel RAM2;
    private javax.swing.JPanel abajo;
    private javax.swing.JPanel arriba1;
    private javax.swing.JPanel arriba2;
    private javax.swing.JLabel ayuda;
    private javax.swing.JPanel barraNav;
    private javax.swing.JProgressBar barraRam1;
    private javax.swing.JProgressBar barraRam2;
    private javax.swing.JPanel barraSuperior;
    private javax.swing.JButton btnConfiguracion;
    private javax.swing.JButton btnCrearBuffer;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnDuracion;
    private javax.swing.JButton btnLog;
    private javax.swing.JButton btnMetricas;
    private javax.swing.JButton btnSimulacion;
    private javax.swing.JLabel clock;
    private javax.swing.JPanel colas1;
    private javax.swing.JPanel colas2;
    private javax.swing.JPanel comp1;
    private javax.swing.JPanel comp2;
    private javax.swing.JPanel filaBuffers;
    private javax.swing.JPanel filaCiclo;
    private javax.swing.JPanel hw1;
    private javax.swing.JPanel hw2;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JList<String> jList3;
    private javax.swing.JList<String> jList4;
    private javax.swing.JList<String> jList5;
    private javax.swing.JList<String> jList6;
    private javax.swing.JList<String> jList7;
    private javax.swing.JList<String> jList8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JLabel lblDuracion;
    private javax.swing.JLabel modo1;
    private javax.swing.JLabel modo2;
    private javax.swing.JPanel panelBuffers;
    private javax.swing.JPanel panelCiclo;
    private javax.swing.JPanel panelComputadores;
    private javax.swing.JPanel panelDuracion;
    private javax.swing.JPanel pcb1Host;
    private javax.swing.JPanel pcb2Host;
    private javax.swing.JPanel pol1Host;
    private javax.swing.JPanel pol2Host;
    private javax.swing.JPanel ramFila1;
    private javax.swing.JPanel ramFila2;
    private javax.swing.JScrollPane scrollBuffers;
    private javax.swing.JLabel sinBuffers;
    private javax.swing.JTextField txtDuracion;
    // End of variables declaration//GEN-END:variables
}
