/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author vyckh
 */

//panel para escoger la politica del computador
public class Panelpolitica extends JPanel{
    private JComboBox<PoliticaPlanificacion> combo;
    private JTextField quantum;
    private JLabel mensaje;
    private Planificador planificador = new FCFS();

    //crea el menu, el cuadro del quantum y el boton
    public Panelpolitica() {
        combo = new JComboBox<>(PoliticaPlanificacion.values());
        quantum = new JTextField("3", 3);
        JButton aplicar = new JButton("Aplicar");
        mensaje = new JLabel("Usando FCFS");

        add(new JLabel("Politica:"));
        add(combo);
        add(new JLabel("Quantum:"));
        add(quantum);
        add(aplicar);
        add(mensaje);

        aplicar.addActionListener(e -> aplicar());
    }
//para cuando le doy al boto se cambia la politica
    private void aplicar() {
        try {
            int q = Integer.parseInt(quantum.getText().trim());
            PoliticaPlanificacion politica = (PoliticaPlanificacion) combo.getSelectedItem();
            planificador = SelectorPolitica.crear(politica, q);
            mensaje.setText("Usando " + politica);
        } catch (NumberFormatException e) {
            mensaje.setText("El quantum debe ser un numero");
        } catch (IllegalArgumentException e) {
            mensaje.setText(e.getMessage());
        }
    }
//devuelve la politica que se esta usando
    public Planificador getPlanificador() {
        return planificador;
    }
}
