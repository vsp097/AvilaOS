/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
//lo que el computador le pide al sistema operativo para usar el area de intercambio
//suspender y reactivar son solicitudes de E/S que pasan por la cola del disco
public interface ServicioIntercambio {

    //true si el proceso tiene su solicitud esperando en la cola y no es la que el disco esta atendiendo
    boolean puedeSuspender(PCB p);

    //reserva el area de intercambio, saca la solicitud de archivo de la cola y encola un SUSPENDER
    //devuelve null si se pudo, o el motivo si no
    String suspender(PCB p, long ciclo);

    //encola un REACTIVAR que lee los bloques del area de intercambio del proceso
    void reactivar(PCB p, long ciclo);

    //suelta los bloques del area de intercambio (cuando un suspendido termina sin volver a la RAM)
    void liberarSwap(PCB p);
}
