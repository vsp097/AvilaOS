/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author vyckh
 */
public class panelPCB extends JPanel{
        private JLabel datos;

    //crea el panel con un titulo y un texto vacio
    public panelPCB() {
        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createTitledBorder("PCB"));
        datos = new JLabel("Ningun proceso seleccionado");
        add(datos);
    }

    //para los datos del proceso
    public void mostrar(PCB p) {
        if (p == null) {
            datos.setText("Ningun proceso seleccionado");
            return;
        }
        datos.setText("<html>"
                + "ID: " + p.getId() + "<br>"
                + "Nombre: " + p.getname() + "<br>"
                + "Estado: " + p.getstate() + "<br>"
                + "Tipo: " + p.gettype() + "<br>"
                + "PC: " + p.getpc() + "<br>"
                + "Memoria: " + p.getmem() + " MB<br>"
                + "Deadline: " + p.getdeadline() + "<br>"
                + "Tiempo restante: " + p.getime()
                + "</html>");
    }
}
