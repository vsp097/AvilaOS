/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package interfaz;

import avilaos.Disco;
import avilaos.Metricas;
import avilaos.PoliticaDisco;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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

    private Metricas metricas;
    //el disco solo se mira para leer sus numeros
    private Disco disco;

    //una linea por grafica del computador: utilizacion, throughput, tasa de exito y tiempo de espera
    private XYSeries[] series = new XYSeries[4];
    //una linea por politica de disco en la grafica del cabezal
    private XYSeries[] seriesCabezal = new XYSeries[PoliticaDisco.values().length];
    //celdas de la tabla del computador
    private JLabel[] celdas = new JLabel[5];
    //celdas de la tabla del disco y la suspension
    private JLabel[] celdasDisco;
    //cada cuanto se refresca el panel
    private Timer timer;

    /**
     * Creates new form PanelMetricas
     */
    public PanelMetricas() {
        this(new Metricas(), null);
    }

    //recibe las metricas del sistema y el disco
    public PanelMetricas(Metricas metricas, Disco disco) {
        initComponents();
        this.metricas = metricas;
        this.disco = disco;
        removeAll();
        //las tablas arriba y las graficas abajo
        setLayout(new BorderLayout(8, 8));
        add(pnlTablas, BorderLayout.NORTH);
        add(pnlGrafica, BorderLayout.CENTER);
        armarTablas();
        armarGraficas();
        //refresco el panel cada segundo
        timer = new Timer(1000, e -> actualizar());
        timer.start();
        actualizar();
    }

    //arma las tablas de numeros dentro de pnlTablas
    private void armarTablas() {
        pnlTablas.setLayout(new BoxLayout(pnlTablas, BoxLayout.Y_AXIS));
        String[] titulos = {"Utilizacion", "Throughput", "Respuesta prom.", "Deadlines", "Equidad"};
        JPanel cpu = crearTabla(titulos, celdas);
        cpu.setBorder(BorderFactory.createTitledBorder("Computador"));
        pnlTablas.add(cpu);

        PoliticaDisco[] politicas = PoliticaDisco.values();
        String[] titulosDisco = new String[4 + politicas.length];
        titulosDisco[0] = "Desplazamiento";
        titulosDisco[1] = "Espera prom. (ciclos)";
        for (int i = 0; i < politicas.length; i++) {
            titulosDisco[2 + i] = "Atendidas " + politicas[i];
        }
        titulosDisco[2 + politicas.length] = "Suspensiones";
        titulosDisco[3 + politicas.length] = "Reactivaciones";
        celdasDisco = new JLabel[titulosDisco.length];
        JPanel disk = crearTabla(titulosDisco, celdasDisco);
        disk.setBorder(BorderFactory.createTitledBorder("Disco y suspension"));
        pnlTablas.add(disk);
    }

    //una fila de titulos y debajo una fila de valores
    private JPanel crearTabla(String[] titulos, JLabel[] destino) {
        JPanel p = new JPanel(new GridLayout(2, titulos.length, 5, 3));
        for (int j = 0; j < titulos.length; j++) {
            p.add(new JLabel("<html><b>" + titulos[j] + "</b></html>", SwingConstants.CENTER));
        }
        for (int j = 0; j < titulos.length; j++) {
            destino[j] = new JLabel("-", SwingConstants.CENTER);
            p.add(destino[j]);
        }
        return p;
    }

    //arma las graficas en pestanas dentro de pnlGrafica
    private void armarGraficas() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Cabezal del disco", graficaCabezal());
        pestanas.addTab("Utilizacion", grafica(Metricas.UTILIZACION, "Utilizacion de la CPU", "Utilizacion (%)"));
        pestanas.addTab("Throughput", grafica(Metricas.THROUGHPUT, "Throughput", "Procesos por ciclo"));
        pestanas.addTab("Tasa de exito", grafica(Metricas.EXITO, "Deadlines cumplidos", "Cumplidos (%)"));
        pestanas.addTab("Tiempo de espera", grafica(Metricas.ESPERA, "Tiempo de respuesta promedio", "Ciclos"));
        pnlGrafica.setLayout(new BorderLayout());
        pnlGrafica.add(pestanas, BorderLayout.CENTER);
    }

    //una grafica con una sola linea
    private ChartPanel grafica(int cual, String titulo, String eje) {
        series[cual] = new XYSeries(titulo);
        JFreeChart g = ChartFactory.createXYLineChart(titulo, "Ciclo", eje, new XYSeriesCollection(series[cual]),
                PlotOrientation.VERTICAL, false, true, false);
        //los porcentajes van de 0 a 100
        if (cual == Metricas.UTILIZACION || cual == Metricas.EXITO) {
            g.getXYPlot().getRangeAxis().setRange(0, 100);
        }
        return new ChartPanel(g);
    }

    //el desplazamiento acumulado contra el ciclo, con un color por politica:
    //si se cambia la politica en ejecucion se ve en que tramo la curva sube mas rapido
    private ChartPanel graficaCabezal() {
        XYSeriesCollection datos = new XYSeriesCollection();
        PoliticaDisco[] politicas = PoliticaDisco.values();
        for (int i = 0; i < politicas.length; i++) {
            //sin ordenar y con x repetidas, porque cada tramo se dibuja tal como llego
            seriesCabezal[i] = new XYSeries(politicas[i].toString(), false, true);
            datos.addSeries(seriesCabezal[i]);
        }
        JFreeChart g = ChartFactory.createXYLineChart("Desplazamiento acumulado del cabezal", "Ciclo",
                "Bloques recorridos", datos, PlotOrientation.VERTICAL, true, true, false);
        //un color fijo y bien distinto por politica, sobre fondo blanco, para poder compararlas
        java.awt.Color[] colores = {
            new java.awt.Color(214, 39, 40), new java.awt.Color(31, 119, 180), new java.awt.Color(44, 160, 44),
            new java.awt.Color(255, 127, 14), new java.awt.Color(148, 103, 189), new java.awt.Color(23, 190, 207)
        };
        org.jfree.chart.plot.XYPlot plot = g.getXYPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(java.awt.Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);
        org.jfree.chart.renderer.xy.XYItemRenderer r = plot.getRenderer();
        for (int i = 0; i < politicas.length; i++) {
            r.setSeriesPaint(i, colores[i % colores.length]);
            r.setSeriesStroke(i, new java.awt.BasicStroke(2.5f));
        }
        return new ChartPanel(g);
    }

    //lee las metricas y actualiza los numeros y las graficas
    private void actualizar() {
        celdas[0].setText(String.format("%.1f%%", metricas.getUtilizacion()));
        celdas[1].setText(String.format("%.3f", metricas.getThroughput()));
        celdas[2].setText(String.format("%.1f", metricas.getRespuestaPromedio()));
        celdas[3].setText(String.format("%.1f%%", metricas.getCumplimientoDeadline()));
        celdas[4].setText(String.format("%.2f", metricas.getEquidad()));

        PoliticaDisco[] politicas = PoliticaDisco.values();
        if (disco != null) {
            celdasDisco[0].setText(disco.getDesplazamientoTotal() + " bloques");
            celdasDisco[1].setText(String.format("%.1f", disco.getEsperaPromedio()));
            for (int i = 0; i < politicas.length; i++) {
                celdasDisco[2 + i].setText(String.valueOf(disco.getAtendidas(politicas[i])));
            }
        }
        celdasDisco[2 + politicas.length].setText(String.valueOf(metricas.getSuspensiones()));
        celdasDisco[3 + politicas.length].setText(String.valueOf(metricas.getReactivaciones()));

        //las graficas del computador se vuelven a llenar con las muestras guardadas
        int porMuestra = metricas.getCiclosPorMuestra();
        int cantidad = metricas.getCantidadMuestras();
        for (int g = 0; g < series.length; g++) {
            series[g].clear();
            for (int i = 0; i < cantidad; i++) {
                series[g].add((i + 1) * porMuestra, metricas.getMuestra(g, i), false);
            }
            series[g].fireSeriesChanged();
        }
        actualizarCabezal();
    }

    //cada punto va a la linea de la politica que estaba activa; cuando cambia, el punto del cambio
    //se pone en las dos lineas para que la curva no quede cortada
    private void actualizarCabezal() {
        int n = metricas.getCantidadMuestrasDisco();
        long[] ciclos = new long[n];
        long[] desplazamientos = new long[n];
        PoliticaDisco[] politicas = new PoliticaDisco[n];
        n = metricas.copiarMuestrasDisco(ciclos, desplazamientos, politicas);
        for (int i = 0; i < seriesCabezal.length; i++) {
            seriesCabezal[i].clear();
        }
        for (int i = 0; i < n; i++) {
            int p = politicas[i].ordinal();
            if (i > 0 && politicas[i - 1] != politicas[i]) {
                seriesCabezal[p].add(ciclos[i - 1], desplazamientos[i - 1], false);
            }
            seriesCabezal[p].add(ciclos[i], desplazamientos[i], false);
        }
        for (int i = 0; i < seriesCabezal.length; i++) {
            seriesCabezal[i].fireSeriesChanged();
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
