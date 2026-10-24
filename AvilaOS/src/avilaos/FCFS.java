/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author vyckh
 */
public class FCFS implements Planificador{
    //el que llega primero a la cola de listos es el primero en usar la CPU
    public PCB elegirSiguiente(Cola<PCB> listos) {
        return listos.Desencolar();
    }
//avisa si el proceso debe salir de la CPU
    public boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados) {
        return false;
    }
}
