/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

import avilaos.Metricas;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author vyckh
 */
public class PanelMetricas extends javax.swing.JPanel {

    /**
     * Creates new form PanelMetricas
     */
        private Metricas metricas;
    private int computadores;
    private int buffers;

    //una linea de la grafica por cada computador
    private XYSeries[] series;
    //celdas de la tabla de computadores: una fila por computador y una ultima para el sistema
    private JLabel[][] celdas;
    //celdas de la tabla de buffers
    private JLabel[][] celdasBuffer;
    //cada cuanto se refresca el panel
    private Timer timer;

    //constructor vacio por si otra ventana ya lo usa
    public PanelMetricas() {
        this(new Metricas(), 2, 0);
    }

    //recibe las metricas del sistema, cuantos computadores hay y cuantos buffers
    public PanelMetricas(Metricas metricas, int computadores, int buffers) {
        initComponents();
        this.metricas = metricas;
        this.computadores = Math.max(1, Math.min(computadores, Metricas.MAX_COMPUTADORES));
        this.buffers = Math.max(0, Math.min(buffers, Metricas.MAX_BUFFERS));
                removeAll();
        setLayout(new BorderLayout(5, 5));
        add(pnlTablas, BorderLayout.NORTH);
        add(pnlGrafica, BorderLayout.CENTER);
        armarTablas();
        armarGrafica();
        //refresco el panel cada segundo
        timer = new Timer(1000, e -> actualizar());
        timer.start();
        actualizar();
    }

    //arma las tablas de numeros dentro de pnlTablas
    private void armarTablas() {
        pnlTablas.setLayout(new BoxLayout(pnlTablas, BoxLayout.Y_AXIS));

        String[] titulos = {"", "Utilizacion", "Throughput", "Respuesta prom.", "Deadlines", "Equidad"};
        String[] nombres = new String[computadores + 1];
        for (int i = 0; i < computadores; i++) {
            nombres[i] = "Computador " + (i + 1);
        }
        nombres[computadores] = "Sistema";
        celdas = new JLabel[nombres.length][titulos.length - 1];
        pnlTablas.add(crearTabla(titulos, nombres, celdas));

        if (buffers > 0) {
            String[] titulosB = {"", "Producidos", "Consumidos", "Bloqueo prom."};
            String[] nombresB = new String[buffers];
            for (int i = 0; i < buffers; i++) {
                nombresB[i] = "Buffer " + (i + 1);
            }
            celdasBuffer = new JLabel[nombresB.length][titulosB.length - 1];
            JPanel tablaB = crearTabla(titulosB, nombresB, celdasBuffer);
            tablaB.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            pnlTablas.add(tablaB);
        }
    }

    //arma la grafica con todas las cpu en el mismo grafico dentro de pnlGrafica
    private void armarGrafica() {
        XYSeriesCollection datos = new XYSeriesCollection();
        series = new XYSeries[computadores];
        for (int i = 0; i < computadores; i++) {
            series[i] = new XYSeries("Computador " + (i + 1));
            datos.addSeries(series[i]);
        }
        JFreeChart grafica = ChartFactory.createXYLineChart(
                "Utilizacion de la CPU", "Ciclo", "Utilizacion (%)",
                datos, PlotOrientation.VERTICAL, true, true, false);
        //la utilizacion siempre va de 0 a 100
        grafica.getXYPlot().getRangeAxis().setRange(0, 100);
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(new ChartPanel(grafica), BorderLayout.CENTER);
    }

    //arma una tabla con una fila de titulos y una fila por cada nombre
    private JPanel crearTabla(String[] titulos, String[] nombres, JLabel[][] destino) {
        JPanel p = new JPanel(new GridLayout(nombres.length + 1, titulos.length, 5, 3));
        for (int j = 0; j < titulos.length; j++) {
            p.add(new JLabel("<html><b>" + titulos[j] + "</b></html>", SwingConstants.CENTER));
        }
        for (int i = 0; i < nombres.length; i++) {
            p.add(new JLabel(nombres[i], SwingConstants.CENTER));
            for (int j = 0; j < titulos.length - 1; j++) {
                destino[i][j] = new JLabel("-", SwingConstants.CENTER);
                p.add(destino[i][j]);
            }
        }
        return p;
    }

    //lee las metricas y actualiza los numeros y la grafica
    private void actualizar() {
        for (int c = 0; c < computadores; c++) {
            celdas[c][0].setText(String.format("%.1f%%", metricas.getUtilizacion(c)));
            celdas[c][1].setText(String.format("%.3f", metricas.getThroughput(c)));
            celdas[c][2].setText(String.format("%.1f", metricas.getRespuestaPromedio(c)));
            celdas[c][3].setText(String.format("%.1f%%", metricas.getCumplimientoDeadline(c)));
            celdas[c][4].setText(String.format("%.2f", metricas.getEquidad(c)));
        }
        //la ultima fila es todo el sistema junto
        int s = computadores;
        celdas[s][0].setText(String.format("%.1f%%", metricas.getUtilizacionSistema()));
        celdas[s][1].setText(String.format("%.3f", metricas.getThroughputSistema()));
        celdas[s][2].setText(String.format("%.1f", metricas.getRespuestaPromedioSistema()));
        celdas[s][3].setText(String.format("%.1f%%", metricas.getCumplimientoDeadlineSistema()));
        celdas[s][4].setText(String.format("%.2f", metricas.getEquidadSistema()));

        for (int b = 0; b < buffers; b++) {
            celdasBuffer[b][0].setText(String.valueOf(metricas.getProducidos(b)));
            celdasBuffer[b][1].setText(String.valueOf(metricas.getConsumidos(b)));
            celdasBuffer[b][2].setText(String.format("%.1f", metricas.getBloqueoPromedio(b)));
        }

        //vuelvo a llenar cada linea de la grafica con las muestras guardadas
        int ciclosPorMuestra = metricas.getCiclosPorMuestra();
        for (int c = 0; c < computadores; c++) {
            series[c].clear();
            int cantidad = metricas.getCantidadMuestras(c);
            for (int i = 0; i < cantidad; i++) {
                series[c].add((i + 1) * ciclosPorMuestra, metricas.getMuestra(c, i), false);
            }
            series[c].fireSeriesChanged();
        }
    }

    //por si la ventana se cierra y no hay que seguir refrescando
    public void detener() {
        timer.stop();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTablas = new javax.swing.JPanel();
        pnlGrafica = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout pnlTablasLayout = new javax.swing.GroupLayout(pnlTablas);
        pnlTablas.setLayout(pnlTablasLayout);
        pnlTablasLayout.setHorizontalGroup(
            pnlTablasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        pnlTablasLayout.setVerticalGroup(
            pnlTablasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        add(pnlTablas, java.awt.BorderLayout.PAGE_START);

        javax.swing.GroupLayout pnlGraficaLayout = new javax.swing.GroupLayout(pnlGrafica);
        pnlGrafica.setLayout(pnlGraficaLayout);
        pnlGraficaLayout.setHorizontalGroup(
            pnlGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        pnlGraficaLayout.setVerticalGroup(
            pnlGraficaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        add(pnlGrafica, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlGrafica;
    private javax.swing.JPanel pnlTablas;
    // End of variables declaration//GEN-END:variables
}
