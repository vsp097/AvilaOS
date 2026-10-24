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
 * @author andrea
 */
public class PanelMetricas extends JPanel {

    private final Metricas metricas;
    private final int computadores;
    private final int buffers;

    //una linea de la grafica por cada computador
    private XYSeries[] series;
    //celdas de la tabla de computadores: una fila por computador y una ultima para el sistema
    private JLabel[][] celdas;
    //celdas de la tabla de buffers
    private JLabel[][] celdasBuffer;
    //cada cuanto se refresca el panel, en milisegundos
    private Timer timer;

    //recibe las metricas del sistema, cuantos computadores hay y cuantos buffers
    public PanelMetricas(Metricas metricas, int computadores, int buffers) {
        this.metricas = metricas;
        this.computadores = Math.max(1, Math.min(computadores, Metricas.MAX_COMPUTADORES));
        this.buffers = Math.max(0, Math.min(buffers, Metricas.MAX_BUFFERS));

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        //arriba van las dos tablas
        JPanel tablas = new JPanel();
        tablas.setLayout(new BoxLayout(tablas, BoxLayout.Y_AXIS));

        String[] titulos = {"", "Utilizacion", "Throughput", "Respuesta prom.", "Deadlines", "Equidad"};
        String[] nombres = new String[this.computadores + 1];
        for (int i = 0; i < this.computadores; i++) {
            nombres[i] = "Computador " + (i + 1);
        }
        nombres[this.computadores] = "Sistema";
        celdas = new JLabel[nombres.length][titulos.length - 1];
        tablas.add(crearTabla(titulos, nombres, celdas));

        if (this.buffers > 0) {
            String[] titulosB = {"", "Producidos", "Consumidos", "Bloqueo prom."};
            String[] nombresB = new String[this.buffers];
            for (int i = 0; i < this.buffers; i++) {
                nombresB[i] = "Buffer " + (i + 1);
            }
            celdasBuffer = new JLabel[nombresB.length][titulosB.length - 1];
            JPanel tablaB = crearTabla(titulosB, nombresB, celdasBuffer);
            tablaB.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            tablas.add(tablaB);
        }
        add(tablas, BorderLayout.NORTH);

        //la grafica: todas las cpu en el mismo grafico
        XYSeriesCollection datos = new XYSeriesCollection();
        series = new XYSeries[this.computadores];
        for (int i = 0; i < this.computadores; i++) {
            series[i] = new XYSeries("Computador " + (i + 1));
            datos.addSeries(series[i]);
        }
        JFreeChart grafica = ChartFactory.createXYLineChart(
                "Utilizacion de la CPU", "Ciclo", "Utilizacion (%)",
                datos, PlotOrientation.VERTICAL, true, true, false);
        //la utilizacion siempre va de 0 a 100
        grafica.getXYPlot().getRangeAxis().setRange(0, 100);
        add(new ChartPanel(grafica), BorderLayout.CENTER);

        //refresco el panel cada segundo
        timer = new Timer(1000, e -> actualizar());
        timer.start();
        actualizar();
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
}