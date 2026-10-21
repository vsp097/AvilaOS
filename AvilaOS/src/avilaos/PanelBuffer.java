/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
/**
 *
 * @author vyckh
 */
//muestra el buffer en la ventana (casillas, semaforos y cuantos esperan)
public class PanelBuffer extends JPanel {

    private Buffer buffer;
    private JLabel titulo;
    private JLabel[] casillas;
    private JLabel semaforos;
    private JLabel esperando;
    //el titulo arriba, las casillas en el medio y los semaforos abajo
    public PanelBuffer(Buffer buffer) {
        this.buffer = buffer;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        titulo = new JLabel();
        add(titulo, BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, buffer.getCapacidad(), 4, 4));
        casillas = new JLabel[buffer.getCapacidad()];
        //una casilla por cada espacio del buffer
        for (int i = 0; i < casillas.length; i++) {
            casillas[i] = new JLabel("", SwingConstants.CENTER);
            casillas[i].setOpaque(true);
            casillas[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            casillas[i].setPreferredSize(new Dimension(44, 44));
            fila.add(casillas[i]);
        }
        add(fila, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new GridLayout(2, 1));
        semaforos = new JLabel();
        esperando = new JLabel();
        abajo.add(semaforos);
        abajo.add(esperando);
        add(abajo, BorderLayout.SOUTH);

        actualizar();
    }
//vuelve a leer el buffer y pinta todo de nuevo, hay que llamarlo cada ciclo
    public void actualizar() {
        titulo.setText(buffer.getNombre() + "   (" + buffer.getCantidad() + " / " + buffer.getCapacidad() + ")");
        //si la casilla tiene algo se pone naranja con el numero, si no queda blanca
        for (int i = 0; i < casillas.length; i++) {
            if (buffer.estaOcupada(i)) {
                casillas[i].setText(String.valueOf(buffer.getElemento(i)));
                casillas[i].setBackground(new Color(255, 214, 153));
            } else {
                casillas[i].setText("");
                casillas[i].setBackground(Color.WHITE);
            }
        }
        int vacio = buffer.getVacio().getValor();
        int lleno = buffer.getLleno().getValor();
        int mutex = buffer.getMutex().getValor();
        semaforos.setText("vacio = " + vacio + "     lleno = " + lleno + "     mutex = " + mutex);
        esperando.setText("Esperando:  " + esperan(vacio) + " en vacio,  " + esperan(lleno) + " en lleno,  " + esperan(mutex) + " en mutex");
    }

    //si el valor del semaforo es negativo, ese numero (sin el menos) es cuantos procesos estan esperando
    private int esperan(int valor) {
        if (valor < 0) {
            return -valor;
        }
        return 0;
    }
}