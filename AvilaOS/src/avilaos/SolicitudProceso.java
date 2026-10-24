package avilaos;

import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
public class SolicitudProceso {

    //el proceso ya armado con los datos que lleno el usuario
    private PCB pcb;
    //numero del computador donde se va a crear (1, 2, 3...)
    private int computador;
    //posicion del buffer en la lista, -1 si el proceso no usa buffer
    private int buffer;
    //solo para cpu bound e i/o bound: ciclos que necesita para terminar
    private int ciclos;
    //solo para productor y consumidor: cada cuantos ciclos produce o consume
    private int cadaCuantosCiclos;
    //solo para productor y consumidor: cuantos elementos necesita para terminar
    private int elementos;

    public SolicitudProceso(PCB pcb, int computador, int buffer, int ciclos, int cadaCuantosCiclos, int elementos) {
        this.pcb = pcb;
        this.computador = computador;
        this.buffer = buffer;
        this.ciclos = ciclos;
        this.cadaCuantosCiclos = cadaCuantosCiclos;
        this.elementos = elementos;
    }

    public PCB getPcb() {
        return pcb;
    }

    public int getComputador() {
        return computador;
    }

    public int getBuffer() {
        return buffer;
    }

    public int getCiclos() {
        return ciclos;
    }

    public int getCadaCuantosCiclos() {
        return cadaCuantosCiclos;
    }

    public int getElementos() {
        return elementos;
    }
}