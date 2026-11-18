/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;

/**
 *
 * @author vyckh
 */
//lo que el computador le pide al sistema operativo cuando un proceso hace E/S
public interface ServicioES {

    //arma la solicitud y la manda a la cola del disco
    //devuelve null si todo salio bien, o el motivo por el que no se pudo
    String solicitar(PCB p, long ciclo);

    //la CPU se quedo sin procesos listos
    void cpuOciosa();
}
