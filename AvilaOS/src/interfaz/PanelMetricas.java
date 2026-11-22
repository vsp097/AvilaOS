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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Panel de métricas, incluida la pestaña del cabezal (parte de Diego, guía 3
 * del HTML de ventanas).
 *
 * Esta rama no tiene todavía el backend de disco/suspensión que aparece en las
 * capturas, así que el cuadro y las pestañas se dejan listos para recibir los
 * datos mediante setters públicos.
 *
 * @author vyckh
 */
public class PanelMetricas extends javax.swing.JPanel {

    private static final int THROUGHPUT = 0;
    private static final int EXITO = 1;
    private static final int ESPERA = 2;

    private Metricas metricas;
    private int computadores;
    private int buffers;

    // una línea de la gráfica por cada computador
    private XYSeries[] series;
    // una línea por cada gráfica del sistema: throughput, éxito y espera
    private XYSeries[] seriesSistema = new XYSeries[3];
    // celdas de la tabla de computadores: una fila por computador y una última para el sistema
    private JLabel[][] celdas;
    // celdas de la tabla de buffers
    private JLabel[][] celdasBuffer;
    // cada cuánto se refresca el panel
    private Timer timer;

    /**
     * Creates new form PanelMetricas
     */
    public PanelMetricas() {
        this(new Metricas(), 2, 0);
    }

    /**
     * Recibe las métricas del sistema, cuántos computadores hay y cuántos buffers.
     */
    public PanelMetricas(Metricas metricas, int computadores, int buffers) {
        initComponents();
        this.metricas = metricas;
        this.computadores = Math.max(1, Math.min(computadores, Metricas.MAX_COMPUTADORES));
        this.buffers = Math.max(0, Math.min(buffers, Metricas.MAX_BUFFERS));
        armarTablas();
        armarGrafica();
        timer = new Timer(1000, e -> actualizar());
        timer.start();
        actualizar();
    }

    private void armarTablas() {
        pnlTablas.setLayout(new BoxLayout(pnlTablas, BoxLayout.Y_AXIS));

        String[] titulos = {"", "Utilización", "Throughput", "Respuesta prom.", "Deadlines", "Equidad"};
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

    // primera pestaña con todas las CPU juntas; las demás son del sistema completo
    private void armarGrafica() {
        XYSeriesCollection datos = new XYSeriesCollection();
        series = new XYSeries[computadores];
        for (int i = 0; i < computadores; i++) {
            series[i] = new XYSeries("Computador " + (i + 1));
            datos.addSeries(series[i]);
        }
        JFreeChart grafica = ChartFactory.createXYLineChart(
                "Utilización de la CPU", "Ciclo", "Utilización (%)",
                datos, PlotOrientation.VERTICAL, true, true, false);
        grafica.getXYPlot().getRangeAxis().setRange(0, 100);
        tabUtilizacion.add(new ChartPanel(grafica), BorderLayout.CENTER);

        tabThroughput.add(graficaSistema(THROUGHPUT, "Throughput del sistema", "Procesos por ciclo"), BorderLayout.CENTER);
        tabExito.add(graficaSistema(EXITO, "Deadlines cumplidos", "Cumplidos (%)"), BorderLayout.CENTER);
        tabEspera.add(graficaSistema(ESPERA, "Tiempo de espera promedio", "Ciclos"), BorderLayout.CENTER);
    }

    private ChartPanel graficaSistema(int cual, String titulo, String eje) {
        seriesSistema[cual] = new XYSeries("Sistema");
        XYSeriesCollection datos = new XYSeriesCollection(seriesSistema[cual]);
        JFreeChart grafica = ChartFactory.createXYLineChart(
                titulo, "Ciclo", eje, datos, PlotOrientation.VERTICAL, false, true, false);
        if (cual == EXITO) {
            grafica.getXYPlot().getRangeAxis().setRange(0, 100);
        }
        return new ChartPanel(grafica);
    }

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

    private void actualizar() {
        for (int c = 0; c < computadores; c++) {
            celdas[c][0].setText(String.format("%.1f%%", metricas.getUtilizacion(c)));
            celdas[c][1].setText(String.format("%.3f", metricas.getThroughput(c)));
            celdas[c][2].setText(String.format("%.1f", metricas.getRespuestaPromedio(c)));
            celdas[c][3].setText(String.format("%.1f%%", metricas.getCumplimientoDeadline(c)));
            celdas[c][4].setText(String.format("%.2f", metricas.getEquidad(c)));
        }
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

    /**
     * Punto de conexión para el cuadro del disco y la suspensión.
     */
    public void setDatosDisco(long bloquesRecorridos, int suspensiones, int reactivaciones) {
        SwingUtilities.invokeLater(() -> {
            lblBloques.setText(String.valueOf(bloquesRecorridos));
            lblSuspensiones.setText(String.valueOf(suspensiones));
            lblReactivaciones.setText(String.valueOf(reactivaciones));
        });
    }

    /**
     * Punto de conexión para las gráficas del sistema: 0 throughput,
     * 1 tasa de éxito, 2 tiempo de espera.
     */
    public void agregarMuestraSistema(int grafica, double ciclo, double valor) {
        if (grafica < 0 || grafica >= seriesSistema.length) {
            return;
        }
        SwingUtilities.invokeLater(() -> seriesSistema[grafica].add(ciclo, valor));
    }

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

        panelSuperior = new javax.swing.JPanel();
        pnlTablas = new javax.swing.JPanel();
        pnlDisco = new javax.swing.JPanel();
        lblBloquesTitulo = new javax.swing.JLabel();
        lblBloques = new javax.swing.JLabel();
        lblSuspensionesTitulo = new javax.swing.JLabel();
        lblSuspensiones = new javax.swing.JLabel();
        lblReactivacionesTitulo = new javax.swing.JLabel();
        lblReactivaciones = new javax.swing.JLabel();
        pnlGrafica = new javax.swing.JPanel();
        tabGraficas = new javax.swing.JTabbedPane();
        tabUtilizacion = new javax.swing.JPanel();
        tabThroughput = new javax.swing.JPanel();
        tabExito = new javax.swing.JPanel();
        tabEspera = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(1000, 620));
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setLayout(new java.awt.GridLayout(1, 2, 8, 8));

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

        panelSuperior.add(pnlTablas);

        pnlDisco.setBorder(javax.swing.BorderFactory.createTitledBorder("Disco y suspensión"));
        pnlDisco.setLayout(new java.awt.GridLayout(3, 2, 6, 4));

        lblBloquesTitulo.setText("Bloques recorridos:");
        pnlDisco.add(lblBloquesTitulo);

        lblBloques.setText("0");
        pnlDisco.add(lblBloques);

        lblSuspensionesTitulo.setText("Suspensiones:");
        pnlDisco.add(lblSuspensionesTitulo);

        lblSuspensiones.setText("0");
        pnlDisco.add(lblSuspensiones);

        lblReactivacionesTitulo.setText("Reactivaciones:");
        pnlDisco.add(lblReactivacionesTitulo);

        lblReactivaciones.setText("0");
        pnlDisco.add(lblReactivaciones);

        panelSuperior.add(pnlDisco);

        add(panelSuperior, java.awt.BorderLayout.NORTH);

        pnlGrafica.setLayout(new java.awt.BorderLayout());

        tabUtilizacion.setLayout(new java.awt.BorderLayout());
        tabGraficas.addTab("Utilización", tabUtilizacion);

        tabThroughput.setLayout(new java.awt.BorderLayout());
        tabGraficas.addTab("Throughput", tabThroughput);

        tabExito.setLayout(new java.awt.BorderLayout());
        tabGraficas.addTab("Tasa de éxito", tabExito);

        tabEspera.setLayout(new java.awt.BorderLayout());
        tabGraficas.addTab("Tiempo de espera", tabEspera);

        pnlGrafica.add(tabGraficas, java.awt.BorderLayout.CENTER);

        add(pnlGrafica, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblBloques;
    private javax.swing.JLabel lblBloquesTitulo;
    private javax.swing.JLabel lblReactivaciones;
    private javax.swing.JLabel lblReactivacionesTitulo;
    private javax.swing.JLabel lblSuspensiones;
    private javax.swing.JLabel lblSuspensionesTitulo;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JPanel pnlDisco;
    private javax.swing.JPanel pnlGrafica;
    private javax.swing.JPanel pnlTablas;
    private javax.swing.JTabbedPane tabGraficas;
    private javax.swing.JPanel tabEspera;
    private javax.swing.JPanel tabExito;
    private javax.swing.JPanel tabThroughput;
    private javax.swing.JPanel tabUtilizacion;
    // End of variables declaration//GEN-END:variables
}
