/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import avilaos.Bloque;
import avilaos.DireccionCabezal;
import avilaos.Disco;
import avilaos.PoliticaDisco;
import avilaos.SolicitudES;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

/**
 *
 * @author andrea
 */
//el disco: todos sus bloques numerados, quien ocupa cada uno y donde esta el cabezal
public class PanelDisco extends JPanel {

    private static final int COLUMNAS = 20;
    private static final int ANCHO = 29;
    private static final int ALTO = 24;
    private static final int MARGEN = 8;
    private static final int SEPARACION = 26;

    private JLabel lblCabezal = new JLabel();
    private JLabel lblEstado = new JLabel();
    private JLabel lblNumeros = new JLabel();
    private JLabel lblAtendidas = new JLabel();
    private Mapa mapa = new Mapa();

    private Bloque[] bloques = new Bloque[0];
    private int bloquesArchivos = 0;
    private int cabezal = 0;

    public PanelDisco() {
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder("Disco"));
        JPanel arriba = new JPanel(new GridLayout(4, 1));
        lblCabezal.setFont(lblCabezal.getFont().deriveFont(Font.BOLD, 13f));
        arriba.add(lblCabezal);
        arriba.add(lblEstado);
        arriba.add(lblNumeros);
        arriba.add(lblAtendidas);
        add(arriba, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(mapa);
        scroll.getVerticalScrollBar().setUnitIncrement(ALTO);
        add(scroll, BorderLayout.CENTER);
        ToolTipManager.sharedInstance().registerComponent(mapa);
    }

    //lee el estado del disco y lo dibuja; el disco no se entera
    public void mostrar(Disco disco) {
        bloques = disco.copiarBloques();
        bloquesArchivos = disco.getBloquesArchivos();
        cabezal = disco.getCabezal();
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
        mapa.revalidate();
        mapa.repaint();
    }

    private class Mapa extends JPanel {

        Mapa() {
            setBackground(Color.WHITE);
        }

        private int filas(int cantidad) {
            return (cantidad + COLUMNAS - 1) / COLUMNAS;
        }

        //donde empieza a dibujarse el area de intercambio
        private int ySwap() {
            return MARGEN + 16 + filas(bloquesArchivos) * ALTO + SEPARACION;
        }

        @Override
        public Dimension getPreferredSize() {
            int swap = bloques.length - bloquesArchivos;
            int alto = ySwap() + filas(swap) * ALTO + MARGEN;
            return new Dimension(MARGEN * 2 + COLUMNAS * ANCHO, alto);
        }

        //en que esquina se dibuja el bloque i
        private int xDe(int i) {
            int pos = i < bloquesArchivos ? i : i - bloquesArchivos;
            return MARGEN + (pos % COLUMNAS) * ANCHO;
        }

        private int yDe(int i) {
            if (i < bloquesArchivos) {
                return MARGEN + 16 + (i / COLUMNAS) * ALTO;
            }
            return ySwap() + ((i - bloquesArchivos) / COLUMNAS) * ALTO;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g.setColor(Color.DARK_GRAY);
            g.drawString("Archivos (bloques 0 a " + (bloquesArchivos - 1) + ")", MARGEN, MARGEN + 10);
            if (bloques.length > bloquesArchivos) {
                g.drawString("Area de intercambio (bloques " + bloquesArchivos + " a " + (bloques.length - 1) + ")",
                        MARGEN, ySwap() - 6);
            }
            g.setFont(getFont().deriveFont(Font.PLAIN, 10f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < bloques.length; i++) {
                Bloque b = bloques[i];
                int x = xDe(i);
                int y = yDe(i);
                Color fondo;
                if (b.esSwap()) {
                    fondo = b.estaOcupado() ? Paleta.SWAP_OCUPADO : Paleta.SWAP_LIBRE;
                } else {
                    fondo = b.estaOcupado() ? Paleta.de(b.getColor()) : Paleta.LIBRE;
                }
                g.setColor(fondo);
                g.fillRect(x + 1, y + 1, ANCHO - 2, ALTO - 2);
                g.setColor(new Color(190, 190, 190));
                g.drawRect(x + 1, y + 1, ANCHO - 3, ALTO - 3);
                g.setColor(b.esSwap() && b.estaOcupado() ? Color.WHITE : Color.BLACK);
                String texto = String.valueOf(b.getNumero());
                g.drawString(texto, x + (ANCHO - fm.stringWidth(texto)) / 2, y + ALTO / 2 + 4);
            }
            //el cabezal es el recuadro rojo
            if (cabezal >= 0 && cabezal < bloques.length) {
                g.setColor(Paleta.CABEZAL);
                g.setStroke(new BasicStroke(3f));
                g.drawRect(xDe(cabezal), yDe(cabezal), ANCHO - 1, ALTO - 1);
            }
        }

        //al pasar el raton por un bloque dice quien lo ocupa y cual es el siguiente de la cadena
        @Override
        public String getToolTipText(MouseEvent e) {
            for (int i = 0; i < bloques.length; i++) {
                int x = xDe(i);
                int y = yDe(i);
                if (e.getX() >= x && e.getX() < x + ANCHO && e.getY() >= y && e.getY() < y + ALTO) {
                    Bloque b = bloques[i];
                    if (!b.estaOcupado()) {
                        return "Bloque " + i + ": libre";
                    }
                    String sig = b.getSiguiente() < 0 ? "es el ultimo de la cadena" : "siguiente: " + b.getSiguiente();
                    return "Bloque " + i + ": " + b.getOcupante() + " (" + sig + ")";
                }
            }
            return null;
        }
    }
}
