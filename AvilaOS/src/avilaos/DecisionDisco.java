/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//lo que responde una politica de disco: o atiende una solicitud, o primero lleva el cabezal a un extremo
public class DecisionDisco {

    private SolicitudES solicitud;
    //bloque del extremo al que hay que llegar antes de seguir, -1 si no hace falta
    private int extremo = -1;
    //bloque al que salta el cabezal al llegar al extremo (C-SCAN), -1 si solo se devuelve
    private int saltoA = -1;

    private DecisionDisco() {
    }

    public static DecisionDisco atender(SolicitudES s) {
        DecisionDisco d = new DecisionDisco();
        d.solicitud = s;
        return d;
    }

    //el cabezal llega al extremo y cambia de direccion
    public static DecisionDisco irAlExtremo(int extremo) {
        DecisionDisco d = new DecisionDisco();
        d.extremo = extremo;
        return d;
    }

    //el cabezal llega al extremo y salta a otro bloque sin atender nada en el camino
    public static DecisionDisco irAlExtremoYSaltar(int extremo, int saltoA) {
        DecisionDisco d = new DecisionDisco();
        d.extremo = extremo;
        d.saltoA = saltoA;
        return d;
    }

    public SolicitudES getSolicitud() {
        return solicitud;
    }

    public int getExtremo() {
        return extremo;
    }

    public int getSaltoA() {
        return saltoA;
    }
}
