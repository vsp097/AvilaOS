/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author vyckh
 */
public class Panellog extends JPanel{
    private LogEventos log;
    private JTextArea area;

    //crea el cuadro de texto donde se ven los eventos
    public Panellog(LogEventos log) {
        this.log = log;
        setLayout(new BorderLayout());
        area = new JTextArea(15, 50);
        //para que no se pueda escribir en el cuadro
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
        actualizar();
    }

    //vuelve a poner todo el texto del log
    public void actualizar() {
        area.setText(log.getTexto());
    }
}
