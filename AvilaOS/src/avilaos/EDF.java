package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
public class EDF implements Planificador {

    //gana el que tiene el deadline mas cercano (el numero mas chico)
    //si el deadline es 0 o menos lo trato como que no tiene deadline y va de ultimo
    private boolean esMasUrgente(PCB a, PCB b) {
        int da = a.getdeadline();
        int db = b.getdeadline();
        if (da <= 0) {
            return false;
        }
        if (db <= 0) {
            return true;
        }
        return da < db;
    }

    //saca de listos el proceso con el deadline mas cercano, si empatan va el que llego primero
    @Override
    public PCB elegirSiguiente(Cola<PCB> listos) {
        Cola<PCB> temporal = new Cola<>();
        PCB mejor = null;

        //recorro la cola buscando el mas urgente y paso todos a una cola temporal
        PCB p = listos.Desencolar();
        while (p != null) {
            if (mejor == null || esMasUrgente(p, mejor)) {
                mejor = p;
            }
            temporal.Encolar(p);
            p = listos.Desencolar();
        }

        //los devuelvo a listos en el mismo orden, menos el elegido
        p = temporal.Desencolar();
        while (p != null) {
            if (p != mejor) {
                listos.Encolar(p);
            }
            p = temporal.Desencolar();
        }
        return mejor;
    }

    //avisa si el proceso de la cpu debe salir porque llego uno con deadline mas cercano
    @Override
    public boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados) {
        if (actual == null) {
            return false;
        }
        boolean sacar = false;
        Cola<PCB> temporal = new Cola<>();

        //reviso si hay alguno mas urgente que el actual
        PCB p = listos.Desencolar();
        while (p != null) {
            if (esMasUrgente(p, actual)) {
                sacar = true;
            }
            temporal.Encolar(p);
            p = listos.Desencolar();
        }

        //dejo la cola igual que estaba
        p = temporal.Desencolar();
        while (p != null) {
            listos.Encolar(p);
            p = temporal.Desencolar();
        }
        return sacar;
    }
}