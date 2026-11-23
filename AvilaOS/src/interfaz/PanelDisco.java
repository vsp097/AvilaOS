/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

//--- bloque a: imports ---
import avilaos.Bloque;
import avilaos.DireccionCabezal;
import avilaos.Disco;
import avilaos.PoliticaDisco;
import avilaos.SolicitudES;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
//--- fin del bloque a: imports ---

/**
 *
 * @author andrea
 */
//el disco: todos sus bloques numerados, quien ocupa cada uno y donde esta el cabezal
public class PanelDisco extends javax.swing.JPanel {

    //--- bloque a: campos ---
    private static final int ANCHO = 29;
    private static final int ALTO = 24;
    private static final Border BORDE = BorderFactory.createLineBorder(new Color(190, 190, 190));
    private static final Border BORDE_CABEZAL = BorderFactory.createLineBorder(Paleta.CABEZAL, 3);

    //un JLabel por bloque: primero los de archivos y despues los del area de intercambio
    private JLabel[] celdas = new JLabel[0];
    //lo que muestra cada celda, para cambiar solo las que cambiaron
    private String[] firmas = new String[0];
    private int bloquesArchivos = -1;
    //--- fin del bloque a: campos ---

    /**
     * Creates new form PanelDisco
     */
    public PanelDisco() {
        initComponents();
        //--- bloque b ---
        scrMapa.getVerticalScrollBar().setUnitIncrement(ALTO);
        //--- fin del bloque b ---
    }

    //--- bloque c ---
    //lee el estado del disco y lo dibuja; el disco no se entera
    public void mostrar(Disco disco) {
        Bloque[] bloques = disco.copiarBloques();
        int cabezal = disco.getCabezal();
        if (bloques.length != celdas.length || disco.getBloquesArchivos() != bloquesArchivos) {
            armarCeldas(bloques.length, disco.getBloquesArchivos());
        }
        for (int i = 0; i < bloques.length; i++) {
            pintar(i, bloques[i], i == cabezal);
        }
        DireccionCabezal dir = disco.getDireccion();
        lblCabezal.setText("Cabezal en el bloque " + cabezal + "  " + (dir == DireccionCabezal.ARRIBA ? "↑" : "↓")
                + "  " + dir + "     Politica: " + disco.getPolitica());
        SolicitudES actual = disco.getActual();
        if (disco.getRetenidas() > 0) {
            lblEstado.setText("Esperando la carga inicial: faltan " + disco.getRetenidas() + " solicitudes por llegar");
        } else if (actual != null) {
            lblEstado.setText("Atendiendo a " + actual.getProceso().getname() + ": " + actual.getOperacion() + " "
                    + actual.getRuta() + " (bloque " + actual.getBloque() + ")");
        } else if (disco.estaOcioso()) {
            lblEstado.setText("Sin solicitudes");
        } else {
            lblEstado.setText("Moviendo el cabezal");
        }
        StringBuilder porPolitica = new StringBuilder();
        PoliticaDisco[] politicas = PoliticaDisco.values();
        for (int i = 0; i < politicas.length; i++) {
            porPolitica.append(i == 0 ? "" : ", ").append(politicas[i]).append(" ").append(disco.getAtendidas(politicas[i]));
        }
        lblNumeros.setText("Desplazamiento total: " + disco.getDesplazamientoTotal() + " bloques     Espera promedio: "
                + String.format("%.1f", disco.getEsperaPromedio()) + " ciclos     Libres: " + disco.getLibres() + " de "
                + bloquesArchivos);
        lblAtendidas.setText("Solicitudes atendidas por politica: " + porPolitica);
    }

    //crea un JLabel por bloque; solo pasa al principio o si cambia el tamano del disco
    private void armarCeldas(int total, int deArchivos) {
        bloquesArchivos = deArchivos;
        pnlMapaArchivos.removeAll();
        pnlMapaSwap.removeAll();
        celdas = new JLabel[total];
        firmas = new String[total];
        for (int i = 0; i < total; i++) {
            JLabel c = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            c.setOpaque(true);
            c.setPreferredSize(new Dimension(ANCHO, ALTO));
            c.setFont(c.getFont().deriveFont(Font.PLAIN, 10f));
            c.setBorder(BORDE);
            celdas[i] = c;
            if (i < deArchivos) {
                pnlMapaArchivos.add(c);
            } else {
                pnlMapaSwap.add(c);
            }
        }
        lblTituloArchivos.setText("Archivos (bloques 0 a " + (deArchivos - 1) + ")");
        boolean haySwap = total > deArchivos;
        lblTituloSwap.setText(haySwap ? "Area de intercambio (bloques " + deArchivos + " a " + (total - 1) + ")" : " ");
        pnlZonaSwap.setVisible(haySwap);
        pnlMapaArchivos.revalidate();
        pnlMapaSwap.revalidate();
    }

    //color de fondo, borde rojo si ahi esta el cabezal, y el tooltip con el ocupante y el siguiente
    private void pintar(int i, Bloque b, boolean esCabezal) {
        String firma = b.estaOcupado() + "|" + b.getColor() + "|" + b.getOcupante() + "|" + b.getSiguiente() + "|" + esCabezal;
        if (firma.equals(firmas[i])) {
            return;
        }
        firmas[i] = firma;
        JLabel c = celdas[i];
        if (b.esSwap()) {
            c.setBackground(b.estaOcupado() ? Paleta.SWAP_OCUPADO : Paleta.SWAP_LIBRE);
            c.setForeground(b.estaOcupado() ? Color.WHITE : Color.BLACK);
        } else {
            c.setBackground(b.estaOcupado() ? Paleta.de(b.getColor()) : Paleta.LIBRE);
            c.setForeground(Color.BLACK);
        }
        c.setBorder(esCabezal ? BORDE_CABEZAL : BORDE);
        if (!b.estaOcupado()) {
            c.setToolTipText("Bloque " + i + ": libre" + (esCabezal ? " (aqui esta el cabezal)" : ""));
        } else {
            String sig = b.getSiguiente() < 0 ? "es el ultimo de la cadena" : "siguiente: " + b.getSiguiente();
            c.setToolTipText("Bloque " + i + ": " + b.getOcupante() + " (" + sig + ")" + (esCabezal ? " - cabezal" : ""));
        }
    }
    //--- fin del bloque c ---

    //--- diseno: esto lo genera el disenador de NetBeans ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        pnlInfo = new javax.swing.JPanel();
        lblCabezal = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblNumeros = new javax.swing.JLabel();
        lblAtendidas = new javax.swing.JLabel();
        scrMapa = new javax.swing.JScrollPane();
        pnlContenido = new javax.swing.JPanel();
        pnlZonaArchivos = new javax.swing.JPanel();
        lblTituloArchivos = new javax.swing.JLabel();
        pnlEnvolturaArchivos = new javax.swing.JPanel();
        pnlMapaArchivos = new javax.swing.JPanel();
        pnlZonaSwap = new javax.swing.JPanel();
        lblTituloSwap = new javax.swing.JLabel();
        pnlEnvolturaSwap = new javax.swing.JPanel();
        pnlMapaSwap = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Disco"));
        setLayout(new java.awt.BorderLayout(0, 4));

        pnlInfo.setLayout(new java.awt.GridLayout(4, 1));

        lblCabezal.setFont(lblCabezal.getFont().deriveFont(lblCabezal.getFont().getStyle() | java.awt.Font.BOLD, lblCabezal.getFont().getSize()+1));
        lblCabezal.setText("Cabezal");
        pnlInfo.add(lblCabezal);

        lblEstado.setText(" ");
        pnlInfo.add(lblEstado);

        lblNumeros.setText(" ");
        pnlInfo.add(lblNumeros);

        lblAtendidas.setText(" ");
        pnlInfo.add(lblAtendidas);

        add(pnlInfo, java.awt.BorderLayout.PAGE_START);

        pnlContenido.setBackground(new java.awt.Color(255, 255, 255));
        pnlContenido.setLayout(new java.awt.BorderLayout());

        pnlZonaArchivos.setBackground(new java.awt.Color(255, 255, 255));
        pnlZonaArchivos.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlZonaArchivos.setLayout(new java.awt.BorderLayout(0, 4));

        lblTituloArchivos.setFont(lblTituloArchivos.getFont().deriveFont(lblTituloArchivos.getFont().getStyle() | java.awt.Font.BOLD, lblTituloArchivos.getFont().getSize()-1));
        lblTituloArchivos.setText("Archivos");
        pnlZonaArchivos.add(lblTituloArchivos, java.awt.BorderLayout.PAGE_START);

        pnlEnvolturaArchivos.setBackground(new java.awt.Color(255, 255, 255));
        pnlEnvolturaArchivos.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        pnlMapaArchivos.setBackground(new java.awt.Color(255, 255, 255));
        pnlMapaArchivos.setLayout(new java.awt.GridLayout(0, 20));
        pnlEnvolturaArchivos.add(pnlMapaArchivos);

        pnlZonaArchivos.add(pnlEnvolturaArchivos, java.awt.BorderLayout.CENTER);

        pnlContenido.add(pnlZonaArchivos, java.awt.BorderLayout.PAGE_START);

        pnlZonaSwap.setBackground(new java.awt.Color(255, 255, 255));
        pnlZonaSwap.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 8, 8, 8));
        pnlZonaSwap.setLayout(new java.awt.BorderLayout(0, 4));

        lblTituloSwap.setFont(lblTituloSwap.getFont().deriveFont(lblTituloSwap.getFont().getStyle() | java.awt.Font.BOLD, lblTituloSwap.getFont().getSize()-1));
        lblTituloSwap.setText("Area de intercambio");
        pnlZonaSwap.add(lblTituloSwap, java.awt.BorderLayout.PAGE_START);

        pnlEnvolturaSwap.setBackground(new java.awt.Color(255, 255, 255));
        pnlEnvolturaSwap.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        pnlMapaSwap.setBackground(new java.awt.Color(255, 255, 255));
        pnlMapaSwap.setLayout(new java.awt.GridLayout(0, 20));
        pnlEnvolturaSwap.add(pnlMapaSwap);

        pnlZonaSwap.add(pnlEnvolturaSwap, java.awt.BorderLayout.CENTER);

        pnlContenido.add(pnlZonaSwap, java.awt.BorderLayout.CENTER);

        scrMapa.setViewportView(pnlContenido);

        add(scrMapa, java.awt.BorderLayout.CENTER);
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel lblAtendidas;
    private javax.swing.JLabel lblCabezal;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblNumeros;
    private javax.swing.JLabel lblTituloArchivos;
    private javax.swing.JLabel lblTituloSwap;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlEnvolturaArchivos;
    private javax.swing.JPanel pnlEnvolturaSwap;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPanel pnlMapaArchivos;
    private javax.swing.JPanel pnlMapaSwap;
    private javax.swing.JPanel pnlZonaArchivos;
    private javax.swing.JPanel pnlZonaSwap;
    private javax.swing.JScrollPane scrMapa;
    // End of variables declaration
    //--- fin del diseno ---
}
