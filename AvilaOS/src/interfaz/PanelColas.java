/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import Data_Structures.EstadoProceso;
import Data_Structures.PCB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 *
 * @author andrea
 */
//las colas del computador: una columna por cada uno de los siete estados
public class PanelColas extends JPanel {

    //para avisarle a la ventana cuando hacen clic en un proceso
    public interface SeleccionListener {
        void procesoElegido(PCB p);
    }

    private static final Color[] FONDOS = {
        new Color(108, 117, 125), new Color(42, 157, 143), new Color(38, 70, 83), new Color(214, 95, 60),
        new Color(87, 140, 190), new Color(150, 110, 180), new Color(90, 90, 90)
    };

    private EstadoProceso[] estados = EstadoProceso.values();
    private JLabel[] titulos = new JLabel[estados.length];
    private JList<String[]>[] listas = new JList[estados.length];
    //los procesos que se estan mostrando en cada columna, en el mismo orden que la lista
    private PCB[][] mostrados = new PCB[estados.length][0];
    //lo ultimo que se dibujo en cada columna, para no redibujar si no cambio nada
    private String[] firmas = new String[estados.length];
    private SeleccionListener listener;

    public PanelColas(SeleccionListener l) {
        listener = l;
        setLayout(new GridLayout(1, estados.length, 6, 0));
        setBorder(BorderFactory.createTitledBorder("Colas del computador"));
        for (int i = 0; i < estados.length; i++) {
            add(armarColumna(i));
        }
    }

    private JPanel armarColumna(final int i) {
        JPanel columna = new JPanel(new BorderLayout());
        titulos[i] = new JLabel(titulo(i, 0), SwingConstants.CENTER);
        titulos[i].setOpaque(true);
        titulos[i].setBackground(FONDOS[i]);
        titulos[i].setForeground(Color.WHITE);
        titulos[i].setFont(titulos[i].getFont().deriveFont(Font.BOLD, 12f));
        titulos[i].setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        columna.add(titulos[i], BorderLayout.NORTH);

        final JList<String[]> lista = new JList<>();
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        listas[i] = lista;
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(FONDOS[i]));
        columna.add(scroll, BorderLayout.CENTER);
        columna.setMinimumSize(new Dimension(110, 150));
        columna.setPreferredSize(new Dimension(160, 210));
        return columna;
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
}
