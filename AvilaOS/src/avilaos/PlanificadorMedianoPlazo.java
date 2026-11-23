/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
//el criterio del planificador a mediano plazo, asi se puede cambiar sin tocar el computador
public interface PlanificadorMedianoPlazo {

    //recibe los bloqueados que se pueden suspender y devuelve el que se saca de la RAM, o null
    //la cola no se modifica
    PCB elegirParaSuspender(Cola<PCB> bloqueados);

    //recibe los listos/suspendidos que caben en la RAM libre y devuelve el que vuelve, o null
    //la cola no se modifica
    PCB elegirParaReactivar(Cola<PCB> listosSuspendidos);
}
