/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
//una solicitud de un proceso al disco, es lo que se guarda en la cola de E/S
public class SolicitudES {

    private PCB proceso;
    private TipoOperacionES operacion;
    private String ruta;
    //los bloques que el cabezal tiene que visitar, en orden; el primero es el que usa el planificador
    private int[] recorrido;
    //cuantos bloques del recorrido ya se leyeron o escribieron
    private int hechos = 0;
    private long cicloLlegada;
    private long cicloInicio = -1;
    //color que se le reservo al archivo cuando la solicitud es de CREAR
    private int color = -1;

    public SolicitudES(PCB proceso, TipoOperacionES operacion, String ruta, int[] recorrido, long cicloLlegada) {
        this.proceso = proceso;
        this.operacion = operacion;
        this.ruta = ruta;
        this.recorrido = recorrido;
        this.cicloLlegada = cicloLlegada;
    }

    public PCB getProceso() {
        return proceso;
    }

    public TipoOperacionES getOperacion() {
        return operacion;
    }

    public String getRuta() {
        return ruta;
    }

    //el bloque donde empieza la solicitud
    public int getBloque() {
        return recorrido[0];
    }

    public int[] getRecorrido() {
        return recorrido;
    }

    public int getHechos() {
        return hechos;
    }

    public void sumarHecho() {
        hechos++;
    }

    public boolean termino() {
        return hechos >= recorrido.length;
    }

    //el bloque que le toca ahora al cabezal
    public int getBloqueActual() {
        return recorrido[hechos];
    }

    public int getColor() {
        return color;
    }

    public void setColor(int c) {
        color = c;
    }

    public long getCicloLlegada() {
        return cicloLlegada;
    }

    public long getCicloInicio() {
        return cicloInicio;
    }

    public void setCicloInicio(long c) {
        cicloInicio = c;
    }
}
