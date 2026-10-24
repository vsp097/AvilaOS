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
public class Roundrobin implements Planificador {
    
    private int quantum;

    public Roundrobin(int quantum) {
        setQuantum(quantum);
    }
//cambia el quantum, que se puede cambiar mientras corre, y que no deja poner menos de 1
    public void setQuantum(int quantum) {
        if (quantum < 1) {
            throw new IllegalArgumentException("El quantum debe ser mayor que 0");
        }
        this.quantum = quantum;
    }
   //devuelve el quantum para mostrarlo en la ventana 
    public int getQuantum() {
        return quantum;
    }
    
//escoge el primero de la cola de listos
    public PCB elegirSiguiente(Cola<PCB> listos) {
        return listos.Desencolar();
    }
//avisa que el proceso sale cuando ya uso todo el quantum, y vuelve al final de la cola
    public boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados) {
        return ciclosUsados >= quantum;
    }
}
