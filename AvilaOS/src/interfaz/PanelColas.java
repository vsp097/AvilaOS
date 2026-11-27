/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import Data_Structures.EstadoProceso;
import Data_Structures.PCB;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
//--- fin del bloque a: imports ---

/**
 *
 * @author andrea
 */
//las colas del computador: una columna por cada uno de los siete estados
public class PanelColas extends javax.swing.JPanel {

    //--- bloque a: campos ---
    //para avisarle a la ventana cuando hacen clic en un proceso
    public interface SeleccionListener {
        void procesoElegido(PCB p);
    }

    //un color por estado, en el mismo orden que el enum EstadoProceso
    private static final Color[] FONDOS = {
        new Color(108, 117, 125), new Color(42, 157, 143), new Color(38, 70, 83), new Color(214, 95, 60),
        new Color(87, 140, 190), new Color(150, 110, 180), new Color(90, 90, 90)
    };

    private EstadoProceso[] estados = EstadoProceso.values();
    //los componentes del disenador puestos en arreglos, en el orden del enum
    private JLabel[] titulos;
    private JList<String[]>[] listas;
    private JScrollPane[] scrolls;
    //los procesos que se estan mostrando en cada columna, en el mismo orden que la lista
    private PCB[][] mostrados = new PCB[estados.length][0];
    //lo ultimo que se dibujo en cada columna, para no redibujar si no cambio nada
    private String[] firmas = new String[estados.length];
    private SeleccionListener listener;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form PanelColas
     */
    //Java no deja crear un arreglo de JList<String[]> con el tipo puesto, por eso se avisa que la conversion es segura
    @SuppressWarnings("unchecked")
    public PanelColas() {
        initComponents();
        //--- bloque b ---
        titulos = new JLabel[]{lblNuevo, lblListo, lblEjecucion, lblBloqueado, lblListoSuspendido, lblBloqueadoSuspendido, lblTerminado};
        listas = new JList[]{lstNuevo, lstListo, lstEjecucion, lstBloqueado, lstListoSuspendido, lstBloqueadoSuspendido, lstTerminado};
        scrolls = new JScrollPane[]{scrNuevo, scrListo, scrEjecucion, scrBloqueado, scrListoSuspendido, scrBloqueadoSuspendido, scrTerminado};
        for (int i = 0; i < estados.length; i++) {
            prepararColumna(i);
        }
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    public PanelColas(SeleccionListener l) {
        this();
        listener = l;
    }

    public void setSeleccionListener(SeleccionListener l) {
        listener = l;
    }

    //los colores de cada columna y el clic sobre un proceso
    private void prepararColumna(final int i) {
        titulos[i].setOpaque(true);
        titulos[i].setBackground(FONDOS[i]);
        titulos[i].setForeground(Color.WHITE);
        titulos[i].setFont(titulos[i].getFont().deriveFont(Font.BOLD, 12f));
        titulos[i].setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        titulos[i].setText(titulo(i, 0));
        scrolls[i].setBorder(BorderFactory.createLineBorder(FONDOS[i]));
        final JList<String[]> lista = listas[i];
        lista.setCellRenderer(new Celda());
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = lista.locationToIndex(e.getPoint());
                if (fila >= 0 && fila < mostrados[i].length && listener != null) {
                    listener.procesoElegido(mostrados[i][fila]);
                }
            }
        });
    }

    private String titulo(int i, int cuantos) {
        String nombre = estados[i] == EstadoProceso.Ejecucion ? "En ejecucion (CPU)" : estados[i].getEtiqueta();
        return nombre + "  [" + cuantos + "]";
    }

    //recibe la copia de las colas (una por estado) y actualiza solo las columnas que cambiaron
    public void mostrar(PCB[][] colas) {
        for (int i = 0; i < estados.length; i++) {
            PCB[] cola = colas[i];
            String[][] filas = new String[cola.length][];
            StringBuilder firma = new StringBuilder();
            for (int j = 0; j < cola.length; j++) {
                filas[j] = new String[]{(j + 1) + ". " + cola[j].getname(), detalle(estados[i], cola[j])};
                firma.append(cola[j].getId()).append('|').append(filas[j][1]).append('\n');
            }
            if (firma.toString().equals(firmas[i])) {
                continue;
            }
            firmas[i] = firma.toString();
            mostrados[i] = cola;
            listas[i].setToolTipText(estados[i] == EstadoProceso.Bloqueado ? "Haz clic en un proceso para ver su PCB completo" : null);
            titulos[i].setText(titulo(i, cola.length));
            listas[i].setListData(filas);
        }
    }

    //la segunda linea de cada proceso depende de la cola donde esta
    private String detalle(EstadoProceso estado, PCB p) {
        switch (estado) {
            case Nuevo:
                return "espera " + p.getmem() + " MB de RAM";
            case Bloqueado:
            case BloqueadoSuspendido:
                //aqui va corto para que quepa; la ruta completa sale en el PCB al hacer clic
                return p.getMotivo() + ": " + p.getOperacion();
            case ListoSuspendido:
                return p.getEnIntercambio() ? "volviendo a la RAM" : "en swap desde el bloque " + p.getPrimerSwap();
            case Terminado:
                return "PC=" + p.getpc();
            default:
                return "PC=" + p.getpc() + "  prioridad " + p.getPrioridad();
        }
    }

    //cada proceso se dibuja en dos lineas: el nombre y debajo el detalle
    private static class Celda extends JPanel implements ListCellRenderer<String[]> {

        private JLabel nombre = new JLabel();
        private JLabel detalle = new JLabel();

        Celda() {
            setLayout(new GridLayout(2, 1));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
            nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 12f));
            detalle.setFont(detalle.getFont().deriveFont(Font.PLAIN, 11f));
            add(nombre);
            add(detalle);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String[]> lista, String[] valor, int indice,
                boolean seleccionado, boolean foco) {
            nombre.setText(valor[0]);
            detalle.setText(valor[1]);
            setBackground(seleccionado ? lista.getSelectionBackground() : Color.WHITE);
            nombre.setForeground(seleccionado ? lista.getSelectionForeground() : Color.BLACK);
            detalle.setForeground(seleccionado ? lista.getSelectionForeground() : new Color(95, 95, 95));
            return this;
        }
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        pnlNuevo = new javax.swing.JPanel();
        lblNuevo = new javax.swing.JLabel();
        scrNuevo = new javax.swing.JScrollPane();
        lstNuevo = new javax.swing.JList<>();
        pnlListo = new javax.swing.JPanel();
        lblListo = new javax.swing.JLabel();
        scrListo = new javax.swing.JScrollPane();
        lstListo = new javax.swing.JList<>();
        pnlEjecucion = new javax.swing.JPanel();
        lblEjecucion = new javax.swing.JLabel();
        scrEjecucion = new javax.swing.JScrollPane();
        lstEjecucion = new javax.swing.JList<>();
        pnlBloqueado = new javax.swing.JPanel();
        lblBloqueado = new javax.swing.JLabel();
        scrBloqueado = new javax.swing.JScrollPane();
        lstBloqueado = new javax.swing.JList<>();
        pnlListoSuspendido = new javax.swing.JPanel();
        lblListoSuspendido = new javax.swing.JLabel();
        scrListoSuspendido = new javax.swing.JScrollPane();
        lstListoSuspendido = new javax.swing.JList<>();
        pnlBloqueadoSuspendido = new javax.swing.JPanel();
        lblBloqueadoSuspendido = new javax.swing.JLabel();
        scrBloqueadoSuspendido = new javax.swing.JScrollPane();
        lstBloqueadoSuspendido = new javax.swing.JList<>();
        pnlTerminado = new javax.swing.JPanel();
        lblTerminado = new javax.swing.JLabel();
        scrTerminado = new javax.swing.JScrollPane();
        lstTerminado = new javax.swing.JList<>();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Colas del computador"));
        setLayout(new java.awt.GridLayout(1, 7, 6, 0));

        pnlNuevo.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlNuevo.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlNuevo.setLayout(new java.awt.BorderLayout());

        lblNuevo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNuevo.setText("Nuevo  [0]");
        pnlNuevo.add(lblNuevo, java.awt.BorderLayout.PAGE_START);

        scrNuevo.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstNuevo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrNuevo.setViewportView(lstNuevo);

        pnlNuevo.add(scrNuevo, java.awt.BorderLayout.CENTER);

        add(pnlNuevo);

        pnlListo.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlListo.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlListo.setLayout(new java.awt.BorderLayout());

        lblListo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblListo.setText("Listo  [0]");
        pnlListo.add(lblListo, java.awt.BorderLayout.PAGE_START);

        scrListo.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstListo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrListo.setViewportView(lstListo);

        pnlListo.add(scrListo, java.awt.BorderLayout.CENTER);

        add(pnlListo);

        pnlEjecucion.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlEjecucion.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlEjecucion.setLayout(new java.awt.BorderLayout());

        lblEjecucion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEjecucion.setText("En ejecucion (CPU)  [0]");
        pnlEjecucion.add(lblEjecucion, java.awt.BorderLayout.PAGE_START);

        scrEjecucion.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstEjecucion.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrEjecucion.setViewportView(lstEjecucion);

        pnlEjecucion.add(scrEjecucion, java.awt.BorderLayout.CENTER);

        add(pnlEjecucion);

        pnlBloqueado.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlBloqueado.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlBloqueado.setLayout(new java.awt.BorderLayout());

        lblBloqueado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBloqueado.setText("Bloqueado  [0]");
        pnlBloqueado.add(lblBloqueado, java.awt.BorderLayout.PAGE_START);

        scrBloqueado.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstBloqueado.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrBloqueado.setViewportView(lstBloqueado);

        pnlBloqueado.add(scrBloqueado, java.awt.BorderLayout.CENTER);

        add(pnlBloqueado);

        pnlListoSuspendido.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlListoSuspendido.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlListoSuspendido.setLayout(new java.awt.BorderLayout());

        lblListoSuspendido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblListoSuspendido.setText("Listo/Suspendido  [0]");
        pnlListoSuspendido.add(lblListoSuspendido, java.awt.BorderLayout.PAGE_START);

        scrListoSuspendido.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstListoSuspendido.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrListoSuspendido.setViewportView(lstListoSuspendido);

        pnlListoSuspendido.add(scrListoSuspendido, java.awt.BorderLayout.CENTER);

        add(pnlListoSuspendido);

        pnlBloqueadoSuspendido.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlBloqueadoSuspendido.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlBloqueadoSuspendido.setLayout(new java.awt.BorderLayout());

        lblBloqueadoSuspendido.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBloqueadoSuspendido.setText("Bloqueado/Suspendido  [0]");
        pnlBloqueadoSuspendido.add(lblBloqueadoSuspendido, java.awt.BorderLayout.PAGE_START);

        scrBloqueadoSuspendido.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstBloqueadoSuspendido.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrBloqueadoSuspendido.setViewportView(lstBloqueadoSuspendido);

        pnlBloqueadoSuspendido.add(scrBloqueadoSuspendido, java.awt.BorderLayout.CENTER);

        add(pnlBloqueadoSuspendido);

        pnlTerminado.setMinimumSize(new java.awt.Dimension(110, 150));
        pnlTerminado.setPreferredSize(new java.awt.Dimension(160, 210));
        pnlTerminado.setLayout(new java.awt.BorderLayout());

        lblTerminado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTerminado.setText("Terminado  [0]");
        pnlTerminado.add(lblTerminado, java.awt.BorderLayout.PAGE_START);

        scrTerminado.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        lstTerminado.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrTerminado.setViewportView(lstTerminado);

        pnlTerminado.add(scrTerminado, java.awt.BorderLayout.CENTER);

        add(pnlTerminado);
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel lblBloqueado;
    private javax.swing.JLabel lblBloqueadoSuspendido;
    private javax.swing.JLabel lblEjecucion;
    private javax.swing.JLabel lblListo;
    private javax.swing.JLabel lblListoSuspendido;
    private javax.swing.JLabel lblNuevo;
    private javax.swing.JLabel lblTerminado;
    private javax.swing.JList<String[]> lstBloqueado;
    private javax.swing.JList<String[]> lstBloqueadoSuspendido;
    private javax.swing.JList<String[]> lstEjecucion;
    private javax.swing.JList<String[]> lstListo;
    private javax.swing.JList<String[]> lstListoSuspendido;
    private javax.swing.JList<String[]> lstNuevo;
    private javax.swing.JList<String[]> lstTerminado;
    private javax.swing.JPanel pnlBloqueado;
    private javax.swing.JPanel pnlBloqueadoSuspendido;
    private javax.swing.JPanel pnlEjecucion;
    private javax.swing.JPanel pnlListo;
    private javax.swing.JPanel pnlListoSuspendido;
    private javax.swing.JPanel pnlNuevo;
    private javax.swing.JPanel pnlTerminado;
    private javax.swing.JScrollPane scrBloqueado;
    private javax.swing.JScrollPane scrBloqueadoSuspendido;
    private javax.swing.JScrollPane scrEjecucion;
    private javax.swing.JScrollPane scrListo;
    private javax.swing.JScrollPane scrListoSuspendido;
    private javax.swing.JScrollPane scrNuevo;
    private javax.swing.JScrollPane scrTerminado;
    // End of variables declaration
    //--- fin del diseno ---
}
