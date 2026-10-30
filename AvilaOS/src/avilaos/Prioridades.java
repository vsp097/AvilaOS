package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
public class Prioridades implements Planificador {

    //el numero menor es el que tiene mas prioridad
    private boolean tieneMasPrioridad(PCB a, PCB b) {
        return a.getPrioridad() < b.getPrioridad();
    }

    //saca de listos el proceso con mejor prioridad, si empatan va el que llego primero
    @Override
    public PCB elegirSiguiente(Cola<PCB> listos) {
        Cola<PCB> temporal = new Cola<>();
        PCB mejor = null;

        //recorro la cola buscando el mejor y paso todos a una cola temporal
        PCB p = listos.Desencolar();
        while (p != null) {
            if (mejor == null || tieneMasPrioridad(p, mejor)) {
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

    //avisa si el proceso de la cpu debe salir porque llego uno con mas prioridad
    @Override
    public boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados) {
        if (actual == null) {
            return false;
        }
        boolean sacar = false;
        Cola<PCB> temporal = new Cola<>();

        //reviso si hay alguno mejor que el actual
        PCB p = listos.Desencolar();
        while (p != null) {
            if (tieneMasPrioridad(p, actual)) {
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