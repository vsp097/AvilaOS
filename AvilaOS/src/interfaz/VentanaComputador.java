package interfaz;

import Data_Structures.Computador;
import Data_Structures.Listas;
import Data_Structures.PCB;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

/**
 * ventana para ver un computador que no cabe en la ventana principal (el 3, el 4...)
 * muestra lo mismo que main muestra para el computador 1 y 2
 *
 * @author vyckh
 */
public class VentanaComputador extends javax.swing.JFrame {

    private Computador computador;
    private JLabel lblModo = new JLabel("Modo:");
    private JLabel lblRam = new JLabel("0 / 0 MB");
    private JProgressBar barraRam = new JProgressBar();
    private panelPCB pcb = new panelPCB();
    private Panelpolitica politica = new Panelpolitica();
    //nuevos, listos, bloqueados y terminados
    private JList<String>[] listas = new JList[4];
    //los procesos de cada lista, para mostrar el pcb del que se le hace clic
    private PCB[][] enLista = new PCB[4][0];
    private PCB seleccionado;
    private Timer reloj;

    public VentanaComputador(Computador c) {
        computador = c;
        setTitle(c.getNombre());
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        //arriba: cpu, modo y ram
        JPanel hardware = new JPanel(new GridLayout(3, 1));
        hardware.setBorder(BorderFactory.createTitledBorder("Hardware"));
        hardware.add(new JLabel("CPU: " + c.getCPU()));
        hardware.add(lblModo);
        JPanel filaRam = new JPanel(new BorderLayout(6, 0));
        filaRam.add(barraRam, BorderLayout.CENTER);
        filaRam.add(lblRam, BorderLayout.EAST);
        hardware.add(filaRam);

        //en el medio: el pcb y la politica
        politica.setComputador(c);
        JPanel medio = new JPanel(new GridLayout(2, 1));
        medio.add(pcb);
        medio.add(politica);

        //abajo: las 4 colas
        String[] titulos = {"Nuevos", "Listos", "Bloqueados", "Terminados"};
        JPanel colas = new JPanel(new GridLayout(1, 4, 6, 0));
        colas.setBorder(BorderFactory.createTitledBorder("Colas"));
        for (int i = 0; i < 4; i++) {
            listas[i] = new JList<>();
            conectarLista(i);
            JScrollPane scroll = new JScrollPane(listas[i]);
            scroll.setBorder(BorderFactory.createTitledBorder(titulos[i]));
            colas.add(scroll);
        }

        JPanel todo = new JPanel(new BorderLayout(0, 6));
        todo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        todo.add(hardware, BorderLayout.NORTH);
        todo.add(medio, BorderLayout.CENTER);
        todo.add(colas, BorderLayout.SOUTH);
        setContentPane(todo);
        setSize(640, 620);

        //se refresca sola cada 300 ms como los otros paneles
        reloj = new Timer(300, e -> actualizar());
        reloj.start();
    }

    //para que se vea la politica que le puso la configuracion
    public void mostrarPolitica() {
        politica.mostrarPolitica();
    }

    //un clic en un proceso muestra su pcb, otro clic en el mismo vuelve a seguir la cpu
    private void conectarLista(int cual) {
        listas[cual].addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int i = listas[cual].getSelectedIndex();
            if (i < 0 || i >= enLista[cual].length) {
                return;
            }
            PCB p = enLista[cual][i];
            seleccionado = p == seleccionado ? null : p;
        });
    }

    //copia las colas con el semaforo del computador tomado, igual que main
    private void actualizar() {
        Listas<String>[] textos = new Listas[4];
        computador.tomar();
        try {
            textos[0] = main.aListas(computador.getNuevos());
            textos[1] = main.aListas(computador.getListos());
            textos[2] = main.aListasMotivo(computador.getBloqueados());
            textos[3] = main.aListas(computador.getTerminados());
            enLista[0] = main.aArreglo(computador.getNuevos());
            enLista[1] = main.aArreglo(computador.getListos());
            enLista[2] = main.aArreglo(computador.getBloqueados());
            enLista[3] = main.aArreglo(computador.getTerminados());
        } finally {
            computador.soltar();
        }
        for (int i = 0; i < 4; i++) {
            main.mostrar(listas[i], textos[i]);
        }
        main.mostrarRam(computador, barraRam, lblRam);
        lblModo.setText("Modo: " + computador.getmode());
        pcb.mostrar(seleccionado != null ? seleccionado : computador.getEnEjecucion());
    }
}
